package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private static final Comparator<ChatThread> MOST_RECENT_FIRST = (a, b) -> {
        if (a.getLastMessageAt() == null && b.getLastMessageAt() == null) return 0;
        if (a.getLastMessageAt() == null) return 1;
        if (b.getLastMessageAt() == null) return -1;
        return b.getLastMessageAt().compareTo(a.getLastMessageAt());
    };

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Finds the existing thread between these two users for this item, or creates one.
    // Filters only on itemId (single-field, no composite index needed) and checks
    // participants client-side since the list of threads per item is always small.
    public void getOrCreateThread(String itemId, String itemTitle, String posterId, String claimantId,
                                   FirestoreCallback<ChatThread> callback) {
        db.collection("chatThreads")
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ChatThread existing = doc.toObject(ChatThread.class);
                        List<String> participants = existing.getParticipantIds();
                        if (participants != null && participants.contains(posterId) && participants.contains(claimantId)) {
                            existing.setId(doc.getId());
                            callback.onSuccess(existing);
                            return;
                        }
                    }
                    ChatThread thread = new ChatThread(posterId, claimantId, itemId, itemTitle);
                    db.collection("chatThreads").add(thread)
                            .addOnSuccessListener(ref -> {
                                thread.setId(ref.getId());
                                callback.onSuccess(thread);
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getThread(String threadId, FirestoreCallback<ChatThread> callback) {
        db.collection("chatThreads").document(threadId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError(new IllegalArgumentException("Thread not found"));
                        return;
                    }
                    ChatThread thread = doc.toObject(ChatThread.class);
                    thread.setId(doc.getId());
                    callback.onSuccess(thread);
                })
                .addOnFailureListener(callback::onError);
    }

    public ListenerRegistration listenThreadsForUser(String userId, FirestoreListCallback<ChatThread> callback) {
        return db.collection("chatThreads")
                .whereArrayContains("participantIds", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<ChatThread> results = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ChatThread t = doc.toObject(ChatThread.class);
                            t.setId(doc.getId());
                            results.add(t);
                        }
                    }
                    Collections.sort(results, MOST_RECENT_FIRST);
                    callback.onChanged(results);
                });
    }

    public ListenerRegistration listenMessages(String threadId, FirestoreListCallback<Message> callback) {
        return db.collection("chatThreads").document(threadId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<Message> results = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Message m = doc.toObject(Message.class);
                            m.setId(doc.getId());
                            results.add(m);
                        }
                    }
                    callback.onChanged(results);
                });
    }

    public void sendMessage(String threadId, Message message, FirestoreVoidCallback callback) {
        db.collection("chatThreads").document(threadId).collection("messages")
                .add(message)
                .addOnSuccessListener(ref -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("lastMessage", message.getText() != null ? message.getText() : "Sent a photo");
                    update.put("lastMessageAt", FieldValue.serverTimestamp());
                    db.collection("chatThreads").document(threadId).update(update)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }
}
