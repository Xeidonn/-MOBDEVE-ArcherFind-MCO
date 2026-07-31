package com.mobdeve.s17.grp2.archerfind;

import androidx.annotation.Nullable;

public class EditItemFragment extends PostItemFragmentBase {

    @Nullable
    @Override
    protected String getEditingItemId() {
        return getArguments() != null ? getArguments().getString("itemId") : null;
    }

    @Override
    protected String getStatus() {
        // Unused: edit mode in the base class updates the existing item in place and
        // never reads getStatus() to construct a new one.
        return null;
    }

    @Override
    protected String getToolbarTitle() {
        return "Edit Listing";
    }

    @Override
    protected String getSuccessMessage() {
        return "Listing updated!";
    }
}
