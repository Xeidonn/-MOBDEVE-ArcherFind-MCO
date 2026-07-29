package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationRepository {

    private static final Comparator<NotificationItem> NEWEST_FIRST = (a, b) -> {
        if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
        if (a.getCreatedAt() == null) return 1;
        if (b.getCreatedAt() == null) return -1;
        return b.getCreatedAt().compareTo(a.getCreatedAt());
    };

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ListenerRegistration listenForUser(String userId, FirestoreListCallback<NotificationItem> callback) {
        return db.collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<NotificationItem> results = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            NotificationItem n = doc.toObject(NotificationItem.class);
                            n.setId(doc.getId());
                            results.add(n);
                        }
                    }
                    Collections.sort(results, NEWEST_FIRST);
                    callback.onChanged(results);
                });
    }

    public void create(NotificationItem notification, FirestoreVoidCallback callback) {
        db.collection("notifications").add(notification)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void markRead(String notificationId, FirestoreVoidCallback callback) {
        db.collection("notifications").document(notificationId)
                .update("read", true)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
