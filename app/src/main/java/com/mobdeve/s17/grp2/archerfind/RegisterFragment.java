package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.toolbar_register).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        TextInputEditText etFullName = view.findViewById(R.id.et_full_name);
        TextInputEditText etEmail = view.findViewById(R.id.et_reg_email);
        TextInputEditText etStudentId = view.findViewById(R.id.et_student_id);
        TextInputEditText etPassword = view.findViewById(R.id.et_reg_password);
        MaterialButton btnRegister = view.findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String studentId = etStudentId.getText() != null ? etStudentId.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (fullName.isEmpty()) {
                etFullName.setError("Full name is required");
                return;
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                return;
            }
            if (studentId.isEmpty()) {
                etStudentId.setError("Student ID is required");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }

            btnRegister.setEnabled(false);
            authRepository.register(fullName, email, studentId, password, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                    if (!isAdded()) return;
                    Snackbar.make(view, "Account created successfully!", Snackbar.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_register_to_login);
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    btnRegister.setEnabled(true);
                    Snackbar.make(view, "Registration failed: " + message, Snackbar.LENGTH_LONG).show();
                }
            });
        });

        view.findViewById(R.id.btn_go_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }
}
