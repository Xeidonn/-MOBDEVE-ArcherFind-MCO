package com.mobdeve.s17.grp2.archerfind;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemRepository {

    // Firestore composite indexes (equality/array-contains + orderBy on a different field)
    // require manual setup in the console, so queries here filter only and sort client-side.
    private static final Comparator<Item> NEWEST_FIRST = (a, b) -> {
        if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
        if (a.getCreatedAt() == null) return 1;
        if (b.getCreatedAt() == null) return -1;
        return b.getCreatedAt().compareTo(a.getCreatedAt());
    };

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<Item> toSortedItems(QuerySnapshot snapshots) {
        List<Item> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshots) {
            Item item = doc.toObject(Item.class);
            item.setId(doc.getId());
            items.add(item);
        }
        Collections.sort(items, NEWEST_FIRST);
        return items;
    }

    public void createItem(Item item, FirestoreCallback<Item> callback) {
        db.collection("items").add(item)
                .addOnSuccessListener(ref -> ref.get()
                        .addOnSuccessListener(doc -> {
                            Item created = doc.toObject(Item.class);
                            created.setId(doc.getId());
                            callback.onSuccess(created);
                        })
                        .addOnFailureListener(callback::onError))
                .addOnFailureListener(callback::onError);
    }

    public void getItem(String itemId, FirestoreCallback<Item> callback) {
        db.collection("items").document(itemId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError(new IllegalArgumentException("Item not found"));
                        return;
                    }
                    Item item = doc.toObject(Item.class);
                    item.setId(doc.getId());
                    callback.onSuccess(item);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateItem(Item item, FirestoreVoidCallback callback) {
        db.collection("items").document(item.getId()).set(item)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteItem(String itemId, FirestoreVoidCallback callback) {
        db.collection("items").document(itemId).delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void setResolved(String itemId, boolean resolved, FirestoreVoidCallback callback) {
        db.collection("items").document(itemId).update("resolved", resolved)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public ListenerRegistration listenAllItems(FirestoreListCallback<Item> callback) {
        return db.collection("items")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    callback.onChanged(snapshots != null ? toSortedItems(snapshots) : new ArrayList<>());
                });
    }

    public ListenerRegistration listenItemsByStatus(String status, FirestoreListCallback<Item> callback) {
        return db.collection("items")
                .whereEqualTo("status", status)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    callback.onChanged(snapshots != null ? toSortedItems(snapshots) : new ArrayList<>());
                });
    }

    public ListenerRegistration listenItemsByOwner(String ownerId, FirestoreListCallback<Item> callback) {
        return db.collection("items")
                .whereEqualTo("ownerId", ownerId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    callback.onChanged(snapshots != null ? toSortedItems(snapshots) : new ArrayList<>());
                });
    }

    // Unresolved items of the opposite status in the same category — a simple "might be the
    // same item" heuristic used to notify posters of a possible match. Two pure equality
    // filters don't need a composite index (unlike equality + orderBy on a different field).
    public void findPotentialMatches(String category, String oppositeStatus, String excludeOwnerId,
                                      FirestoreListCallback<Item> callback) {
        db.collection("items")
                .whereEqualTo("category", category)
                .whereEqualTo("status", oppositeStatus)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Item> matches = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Item item = doc.toObject(Item.class);
                        item.setId(doc.getId());
                        if (!item.isResolved() && !item.getOwnerId().equals(excludeOwnerId)) {
                            matches.add(item);
                        }
                    }
                    callback.onChanged(matches);
                })
                .addOnFailureListener(callback::onError);
    }

    // Firestore has no native "contains" query, so this fetches once and filters client-side.
    public void searchItems(@Nullable String query, @Nullable String status, @Nullable String category,
                             FirestoreListCallback<Item> callback) {
        db.collection("items").get()
                .addOnSuccessListener(snapshots -> {
                    List<Item> filtered = new ArrayList<>();
                    String q = query == null ? "" : query.toLowerCase().trim();
                    for (Item item : toSortedItems(snapshots)) {
                        if (status != null && !status.equals(item.getStatus())) continue;
                        if (category != null && !category.equals(item.getCategory())) continue;
                        if (!q.isEmpty()) {
                            String haystack = (item.getTitle() + " " + item.getDescription() + " " + item.getLocation()).toLowerCase();
                            if (!haystack.contains(q)) continue;
                        }
                        filtered.add(item);
                    }
                    callback.onChanged(filtered);
                })
                .addOnFailureListener(callback::onError);
    }
}
