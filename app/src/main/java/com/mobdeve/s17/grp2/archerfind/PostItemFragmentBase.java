package com.mobdeve.s17.grp2.archerfind;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Post Lost Item and Post Found Item are identical apart from the status they
// write and their toolbar copy, so the shared form logic lives here once.
public abstract class PostItemFragmentBase extends Fragment {

    protected final ItemRepository itemRepository = new ItemRepository();
    protected final AuthRepository authRepository = new AuthRepository();
    private final SupabaseStorageRepository storageRepository = new SupabaseStorageRepository();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Uri selectedPhotoUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) onPhotoPicked(uri);
            });

    protected abstract String getStatus();
    protected abstract String getToolbarTitle();
    protected abstract String getSuccessMessage();

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

        view.findViewById(R.id.photo_picker).setOnClickListener(v -> photoPickerLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        btnSubmit.setOnClickListener(v ->
                submitItem(view, etTitle, etDescription, etLocation, categoryField, btnSubmit));
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
        String ownerName = user.getDisplayName();
        if (ownerName == null || ownerName.isEmpty()) ownerName = user.getEmail();

        Item item = new Item(title, description, location, category, getStatus(), user.getUid(), ownerName);
        btnSubmit.setEnabled(false);

        if (selectedPhotoUri == null) {
            createItem(view, item, btnSubmit);
        } else {
            uploadPhotoThenCreateItem(view, item, btnSubmit, selectedPhotoUri);
        }
    }

    private void uploadPhotoThenCreateItem(View view, Item item, MaterialButton btnSubmit, Uri photoUri) {
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
                    createItem(view, item, btnSubmit);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
