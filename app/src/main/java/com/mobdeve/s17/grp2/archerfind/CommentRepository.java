package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ListenerRegistration listenForItem(String itemId, FirestoreListCallback<Comment> callback) {
        return db.collection("items").document(itemId).collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<Comment> results = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Comment c = doc.toObject(Comment.class);
                            c.setId(doc.getId());
                            results.add(c);
                        }
                    }
                    callback.onChanged(results);
                });
    }

    public void addComment(Comment comment, FirestoreVoidCallback callback) {
        db.collection("items").document(comment.getItemId()).collection("comments")
                .add(comment)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
