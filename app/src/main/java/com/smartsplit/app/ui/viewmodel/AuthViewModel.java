package com.smartsplit.app.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for authentication state.
 * Wraps FirebaseAuth operations and exposes observable state to AuthActivity.
 */
public class AuthViewModel extends ViewModel {

    /**
     * Represents the state of an auth operation.
     * Pattern: sealed class equivalent in Java using a state holder.
     */
    public enum AuthState {
        IDLE, LOADING, SUCCESS, ERROR
    }

    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Attempts to sign in with email and password.
     * Security: Firebase handles all password hashing — we never store credentials locally.
     */
    public void signIn(String email, String password) {
        if (!validateInput(email, password)) return;

        authState.setValue(AuthState.LOADING);

        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener(result -> authState.setValue(AuthState.SUCCESS))
            .addOnFailureListener(e -> {
                errorMessage.setValue(e.getMessage());
                authState.setValue(AuthState.ERROR);
            });
    }

    /**
     * Attempts to create a new account with email and password.
     */
    public void signUp(String email, String password) {
        if (!validateInput(email, password)) return;

        authState.setValue(AuthState.LOADING);

        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener(result -> authState.setValue(AuthState.SUCCESS))
            .addOnFailureListener(e -> {
                errorMessage.setValue(e.getMessage());
                authState.setValue(AuthState.ERROR);
            });
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    /** Basic client-side validation before making a network call */
    private boolean validateInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email cannot be empty.");
            authState.setValue(AuthState.ERROR);
            return false;
        }
        if (password == null || password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters.");
            authState.setValue(AuthState.ERROR);
            return false;
        }
        return true;
    }
}
