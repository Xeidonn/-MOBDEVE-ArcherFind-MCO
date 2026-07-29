package com.mobdeve.s17.grp2.archerfind;

import java.util.List;

// Fires on every update from a real-time listener, or once for a one-shot list fetch.
public interface FirestoreListCallback<T> {
    void onChanged(List<T> items);
    void onError(Exception e);
}
