package com.smartsplit.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smartsplit.app.databinding.ActivityAuthBinding;
import com.smartsplit.app.ui.MainActivity;
import com.smartsplit.app.ui.viewmodel.AuthViewModel;

/**
 * Single-activity authentication screen.
 * Supports toggle between Login and Signup modes.
 */
public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel authViewModel;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        authViewModel.getAuthState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    binding.btnSignIn.setText("Loading...");
                    binding.btnSignIn.setEnabled(false);
                    binding.btnGoogleSignIn.setEnabled(false);
                    break;
                case SUCCESS:
                    navigateToMain();
                    break;
                case ERROR:
                    binding.btnSignIn.setText(isLoginMode ? "Sign In" : "Create Account");
                    binding.btnSignIn.setEnabled(true);
                    binding.btnGoogleSignIn.setEnabled(true);
                    break;
                case IDLE:
                default:
                    binding.btnSignIn.setText(isLoginMode ? "Sign In" : "Create Account");
                    binding.btnSignIn.setEnabled(true);
                    binding.btnGoogleSignIn.setEnabled(true);
                    break;
            }
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSignIn.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null
                ? binding.etEmail.getText().toString() : "";
            String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";

            if (isLoginMode) {
                authViewModel.signIn(email, password);
            } else {
                authViewModel.signUp(email, password);
            }
        });

        // Toggle between Login and Signup
        binding.tvCreateAccount.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            if (isLoginMode) {
                binding.btnSignIn.setText("Sign In");
                binding.tvCreateAccount.setText("Create account");
                binding.tvTogglePrefix.setText("New to SmartSplit?");
                binding.tvTitle.setText("Welcome Back");
            } else {
                binding.btnSignIn.setText("Create Account");
                binding.tvCreateAccount.setText("Sign in instead");
                binding.tvTogglePrefix.setText("Already have an account?");
                binding.tvTitle.setText("Join SmartSplit");
            }
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
