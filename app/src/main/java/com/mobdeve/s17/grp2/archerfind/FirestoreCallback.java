package com.mobdeve.s17.grp2.archerfind;

// One-shot single-result callback (e.g. fetch one document, create one document).
public interface FirestoreCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
