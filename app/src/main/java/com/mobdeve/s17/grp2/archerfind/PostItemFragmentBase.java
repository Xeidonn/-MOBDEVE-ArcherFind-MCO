package com.mobdeve.s17.grp2.archerfind;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Post Lost Item, Post Found Item, and Edit Listing are identical forms apart from
// the status they write and their toolbar copy, so the shared logic lives here once.
// Edit mode is opted into by overriding getEditingItemId(): when non-null, the form
// is pre-filled from the existing item and submit updates it instead of creating one.
public abstract class PostItemFragmentBase extends Fragment {

    protected final ItemRepository itemRepository = new ItemRepository();
    protected final AuthRepository authRepository = new AuthRepository();
    private final SupabaseStorageRepository storageRepository = new SupabaseStorageRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Uri selectedPhotoUri;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private FusedLocationProviderClient fusedLocationClient;
    private Item editingItem;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) onPhotoPicked(uri);
            });

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    fetchCurrentLocation();
                } else {
                    View root = getView();
                    if (root != null) {
                        Snackbar.make(root, "Location permission is needed to use your current location.", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

    protected abstract String getStatus();
    protected abstract String getToolbarTitle();
    protected abstract String getSuccessMessage();

    // Overridden by EditItemFragment to opt into edit mode.
    @Nullable
    protected String getEditingItemId() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_post);
        toolbar.setTitle(getToolbarTitle());
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        TextInputEditText etTitle = view.findViewById(R.id.et_post_title);
        TextInputEditText etDescription = view.findViewById(R.id.et_post_description);
        TextInputEditText etLocation = view.findViewById(R.id.et_post_location);
        AutoCompleteTextView categoryField = view.findViewById(R.id.spinner_category);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit_post);

        String[] categories = getResources().getStringArray(R.array.item_categories);
        categoryField.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categories));

        view.findViewById(R.id.photo_picker).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add a photo")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        Navigation.findNavController(v).navigate(R.id.action_global_cameraCapture);
                    } else {
                        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    }
                })
                .show());

        NavBackStackEntry currentEntry = Navigation.findNavController(view).getCurrentBackStackEntry();
        if (currentEntry != null) {
            MutableLiveData<String> capturedPhotoLiveData = currentEntry.getSavedStateHandle().getLiveData("captured_photo_uri");
            capturedPhotoLiveData.observe(getViewLifecycleOwner(), uriString -> {
                if (uriString != null) onPhotoPicked(Uri.parse(uriString));
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        view.findViewById(R.id.btn_use_current_location).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        btnSubmit.setOnClickListener(v ->
                submitItem(view, etTitle, etDescription, etLocation, categoryField, btnSubmit));

        String editingItemId = getEditingItemId();
        if (editingItemId != null) {
            loadItemForEditing(view, editingItemId, etTitle, etDescription, etLocation, categoryField, btnSubmit);
        }
    }

    private void loadItemForEditing(View view, String itemId, TextInputEditText etTitle, TextInputEditText etDescription,
                                     TextInputEditText etLocation, AutoCompleteTextView categoryField, MaterialButton btnSubmit) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Loading...");

        itemRepository.getItem(itemId, new FirestoreCallback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (!isAdded()) return;
                editingItem = item;
                etTitle.setText(item.getTitle());
                etDescription.setText(item.getDescription());
                etLocation.setText(item.getLocation());
                categoryField.setText(item.getCategory(), false);
                if (item.hasLocation()) {
                    selectedLatitude = item.getLatitude();
                    selectedLongitude = item.getLongitude();
                    showMapPreview(view, item.getLatitude(), item.getLongitude());
                }
                if (item.getPhotoUrl() != null) {
                    ImageView preview = view.findViewById(R.id.iv_photo_preview);
                    Glide.with(PostItemFragmentBase.this).load(item.getPhotoUrl()).into(preview);
                    preview.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.photo_hint).setVisibility(View.GONE);
                }
                btnSubmit.setText("Save Changes");
                btnSubmit.setEnabled(true);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load item: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("MissingPermission") // only called after an explicit permission check/grant
    private void fetchCurrentLocation() {
        View root = getView();
        if (root == null) return;
        TextView statusView = root.findViewById(R.id.tv_location_status);
        statusView.setText("Detecting location...");

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationTokenSource().getToken())
                .addOnSuccessListener(location -> {
                    if (!isAdded()) return;
                    if (location == null) {
                        statusView.setText("Couldn't detect your location. Try again outdoors or with location enabled.");
                        return;
                    }
                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                    reverseGeocode(location.getLatitude(), location.getLongitude());
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    statusView.setText("Couldn't detect location: " + e.getMessage());
                });
    }

    private void reverseGeocode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        if (Build.VERSION.SDK_INT >= 33) {
            geocoder.getFromLocation(latitude, longitude, 1, addresses -> mainHandler.post(() -> onAddressResolved(addresses)));
        } else {
            ioExecutor.execute(() -> {
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException ignored) {
                    // No connectivity or geocoder unavailable — fall back to raw coordinates below.
                }
                List<Address> result = addresses;
                mainHandler.post(() -> onAddressResolved(result));
            });
        }
    }

    private void onAddressResolved(@Nullable List<Address> addresses) {
        if (!isAdded() || selectedLatitude == null || selectedLongitude == null) return;
        View root = getView();
        if (root == null) return;

        TextView statusView = root.findViewById(R.id.tv_location_status);
        statusView.setText(String.format(Locale.getDefault(), "📍 %.5f, %.5f", selectedLatitude, selectedLongitude));

        if (addresses != null && !addresses.isEmpty() && addresses.get(0).getMaxAddressLineIndex() >= 0) {
            TextInputEditText etLocation = root.findViewById(R.id.et_post_location);
            etLocation.setText(addresses.get(0).getAddressLine(0));
        }

        showMapPreview(root, selectedLatitude, selectedLongitude);
    }

    private void showMapPreview(View root, double latitude, double longitude) {
        if (!StaticMapUtil.isConfigured()) return;
        ImageView mapPreview = root.findViewById(R.id.iv_map_preview);
        Glide.with(mapPreview.getContext()).load(StaticMapUtil.buildUrl(latitude, longitude)).into(mapPreview);
        mapPreview.setVisibility(View.VISIBLE);
    }

    private void onPhotoPicked(Uri uri) {
        selectedPhotoUri = uri;
        View root = getView();
        if (root == null) return;
        ImageView preview = root.findViewById(R.id.iv_photo_preview);
        Glide.with(this).load(uri).into(preview);
        preview.setVisibility(View.VISIBLE);
        root.findViewById(R.id.photo_hint).setVisibility(View.GONE);
    }

    private void submitItem(View view, TextInputEditText etTitle, TextInputEditText etDescription,
                             TextInputEditText etLocation, AutoCompleteTextView categoryField,
                             MaterialButton btnSubmit) {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String category = categoryField.getText() != null ? categoryField.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }
        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            return;
        }
        if (category.isEmpty()) {
            categoryField.setError("Pick a category");
            return;
        }

        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) {
            Snackbar.make(view, "You must be logged in to post an item.", Snackbar.LENGTH_LONG).show();
            return;
        }

        btnSubmit.setEnabled(false);

        if (editingItem != null) {
            editingItem.setTitle(title);
            editingItem.setDescription(description);
            editingItem.setLocation(location);
            editingItem.setCategory(category);
            if (selectedLatitude != null && selectedLongitude != null) {
                editingItem.setLatitude(selectedLatitude);
                editingItem.setLongitude(selectedLongitude);
            }
            saveWithOptionalPhoto(view, editingItem, btnSubmit, () -> updateItem(view, editingItem, btnSubmit));
            return;
        }

        String ownerName = user.getDisplayName();
        if (ownerName == null || ownerName.isEmpty()) ownerName = user.getEmail();

        Item item = new Item(title, description, location, category, getStatus(), user.getUid(), ownerName);
        if (selectedLatitude != null && selectedLongitude != null) {
            item.setLatitude(selectedLatitude);
            item.setLongitude(selectedLongitude);
        }
        saveWithOptionalPhoto(view, item, btnSubmit, () -> createItem(view, item, btnSubmit));
    }

    private void saveWithOptionalPhoto(View view, Item item, MaterialButton btnSubmit, Runnable onReadyToSave) {
        if (selectedPhotoUri == null) {
            onReadyToSave.run();
        } else {
            uploadPhotoThenRun(view, item, btnSubmit, selectedPhotoUri, onReadyToSave);
        }
    }

    private void uploadPhotoThenRun(View view, Item item, MaterialButton btnSubmit, Uri photoUri, Runnable onReadyToSave) {
        String mimeType = requireContext().getContentResolver().getType(photoUri);
        ioExecutor.execute(() -> {
            byte[] bytes;
            try (InputStream in = requireContext().getContentResolver().openInputStream(photoUri)) {
                if (in == null) throw new IOException("Could not open the selected photo");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                bytes = out.toByteArray();
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    btnSubmit.setEnabled(true);
                    Snackbar.make(view, "Failed to read photo: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
                return;
            }

            mainHandler.post(() -> storageRepository.uploadItemPhoto(bytes, mimeType, new FirestoreCallback<String>() {
                @Override
                public void onSuccess(String photoUrl) {
                    if (!isAdded()) return;
                    item.setPhotoUrl(photoUrl);
                    onReadyToSave.run();
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    btnSubmit.setEnabled(true);
                    Snackbar.make(view, "Photo upload failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }));
        });
    }

    private void createItem(View view, Item item, MaterialButton btnSubmit) {
        itemRepository.createItem(item, new FirestoreCallback<Item>() {
            @Override
            public void onSuccess(Item created) {
                if (!isAdded()) return;
                notifyPotentialMatches(created);
                Snackbar.make(view, getSuccessMessage(), Snackbar.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                btnSubmit.setEnabled(true);
                Snackbar.make(view, "Failed to post item: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // Best-effort: notifies posters of unresolved opposite-status items in the same
    // category that a possible match was just posted. Doesn't block navigation on failure.
    private void notifyPotentialMatches(Item created) {
        String oppositeStatus = "Lost".equals(created.getStatus()) ? "Found" : "Lost";
        itemRepository.findPotentialMatches(created.getCategory(), oppositeStatus, created.getOwnerId(),
                new FirestoreListCallback<Item>() {
                    @Override
                    public void onChanged(List<Item> matches) {
                        for (Item match : matches) {
                            NotificationItem notification = new NotificationItem(match.getOwnerId(),
                                    NotificationItem.TYPE_MATCH, "Possible Match Found",
                                    "A new " + created.getStatus().toLowerCase(Locale.ROOT) + " item '" + created.getTitle()
                                            + "' might match your '" + match.getTitle() + "' report.");
                            notification.setRelatedItemId(created.getId());
                            notificationRepository.create(notification, new FirestoreVoidCallback() {
                                @Override
                                public void onSuccess() { /* best-effort */ }

                                @Override
                                public void onError(Exception e) { /* best-effort */ }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) { /* best-effort, matching is not critical */ }
                });
    }

    private void updateItem(View view, Item item, MaterialButton btnSubmit) {
        itemRepository.updateItem(item, new FirestoreVoidCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Snackbar.make(view, "Listing updated!", Snackbar.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                btnSubmit.setEnabled(true);
                Snackbar.make(view, "Failed to update item: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
