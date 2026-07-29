package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate to Manage Listings screen
        view.findViewById(R.id.btn_my_listings).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profile_to_manageListings));

        // Logout clears the Firebase session and the entire back stack
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            authRepository.logout();
            Snackbar.make(v, "Logged out", Snackbar.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_logout_to_login);
        });
    }
}
