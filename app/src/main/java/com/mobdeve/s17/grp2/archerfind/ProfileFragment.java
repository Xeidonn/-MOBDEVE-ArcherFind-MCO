package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ProfileFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();
    private final ItemRepository itemRepository = new ItemRepository();
    private ListenerRegistration listingsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_my_listings).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profile_to_manageListings));

        // Logout clears the Firebase session and the entire back stack
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            authRepository.logout();
            Snackbar.make(v, "Logged out", Snackbar.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_logout_to_login);
        });

        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) return;

        TextView nameView = view.findViewById(R.id.tv_profile_name);
        TextView emailView = view.findViewById(R.id.tv_profile_email);
        TextView idView = view.findViewById(R.id.tv_profile_id);
        TextView listingCountView = view.findViewById(R.id.tv_profile_listing_count);

        nameView.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
        emailView.setText(user.getEmail());
        idView.setText("");

        authRepository.getProfile(user.getUid(), new FirestoreCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (!isAdded() || profile == null) return;
                if (profile.getStudentId() != null) idView.setText("ID: " + profile.getStudentId());
                if (profile.getFullName() != null) nameView.setText(profile.getFullName());
            }

            @Override
            public void onError(Exception e) {
                // No Firestore profile document yet (e.g. it failed to save during registration) —
                // the FirebaseUser fields set above are enough to still show a usable screen.
            }
        });

        listingsListener = itemRepository.listenItemsByOwner(user.getUid(), new FirestoreListCallback<Item>() {
            @Override
            public void onChanged(List<Item> items) {
                if (!isAdded()) return;
                long activeCount = items.stream().filter(item -> !item.isResolved()).count();
                listingCountView.setText(activeCount + " active listing" + (activeCount == 1 ? "" : "s"));
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                listingCountView.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listingsListener != null) {
            listingsListener.remove();
            listingsListener = null;
        }
    }
}
