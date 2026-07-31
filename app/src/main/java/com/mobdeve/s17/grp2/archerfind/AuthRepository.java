package com.mobdeve.s17.grp2.archerfind;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void register(String fullName, String email, String studentId, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError("Registration failed. Please try again.");
                        return;
                    }
                    // Stored on the FirebaseUser itself too, so callers can read the poster's
                    // name synchronously (e.g. when creating an item) without a Firestore round trip.
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullName).build());

                    UserProfile profile = new UserProfile(user.getUid(), fullName, email, studentId);
                    db.collection("users").document(user.getUid()).set(profile)
                            .addOnSuccessListener(unused -> callback.onSuccess(user))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getProfile(String uid, FirestoreCallback<UserProfile> callback) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError(new IllegalArgumentException("Profile not found"));
                        return;
                    }
                    callback.onSuccess(doc.toObject(UserProfile.class));
                })
                .addOnFailureListener(callback::onError);
    }

    public void logout() {
        auth.signOut();
    }

    // Best-effort: registers this device's current push token against the signed-in
    // user so a future server-side sender (e.g. a Cloud Function) could target them.
    public void saveFcmToken(String uid) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token ->
                db.collection("users").document(uid).update("fcmToken", token));
    }
}
