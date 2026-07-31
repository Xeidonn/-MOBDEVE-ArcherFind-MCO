package com.mobdeve.s17.grp2.archerfind;

public class PostFoundItemFragment extends PostItemFragmentBase {

    @Override
    protected String getStatus() {
        return "Found";
    }

    @Override
    protected String getToolbarTitle() {
        return "Report Found Item";
    }

    @Override
    protected String getSuccessMessage() {
        return "Found item reported!";
    }
}
