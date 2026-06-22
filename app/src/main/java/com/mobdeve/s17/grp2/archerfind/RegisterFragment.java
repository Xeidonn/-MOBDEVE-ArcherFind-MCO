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

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar back button navigates back to Login
        view.findViewById(R.id.toolbar_register).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        // Register button shows feedback and navigates back to Login
        view.findViewById(R.id.btn_register).setOnClickListener(v -> {
            Snackbar.make(v, "Account created successfully!", Snackbar.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_register_to_login);
        });

        // "Already have an account" link
        view.findViewById(R.id.btn_go_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }
}
