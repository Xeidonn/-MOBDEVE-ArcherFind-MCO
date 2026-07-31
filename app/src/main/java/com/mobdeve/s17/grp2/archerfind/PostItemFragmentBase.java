package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

// Post Lost Item and Post Found Item are identical apart from the status they
// write and their toolbar copy, so the shared form logic lives here once.
public abstract class PostItemFragmentBase extends Fragment {

    protected final ItemRepository itemRepository = new ItemRepository();
    protected final AuthRepository authRepository = new AuthRepository();

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

        btnSubmit.setOnClickListener(v ->
                submitItem(view, etTitle, etDescription, etLocation, categoryField, btnSubmit));

        // Real capture/picker lands with the photo pipeline; this screen just collects text fields for now.
        view.findViewById(R.id.photo_picker).setOnClickListener(v ->
                Snackbar.make(v, "Camera/gallery would open here", Snackbar.LENGTH_SHORT).show());
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

        btnSubmit.setEnabled(false);
        Item item = new Item(title, description, location, category, getStatus(), user.getUid(), ownerName);
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
}
