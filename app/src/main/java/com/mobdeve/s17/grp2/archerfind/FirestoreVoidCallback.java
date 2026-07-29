package com.mobdeve.s17.grp2.archerfind;

// One-shot callback for writes that don't return data (create/update/delete).
public interface FirestoreVoidCallback {
    void onSuccess();
    void onError(Exception e);
}
