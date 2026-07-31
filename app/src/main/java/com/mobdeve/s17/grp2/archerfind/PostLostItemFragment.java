package com.mobdeve.s17.grp2.archerfind;

public class PostLostItemFragment extends PostItemFragmentBase {

    @Override
    protected String getStatus() {
        return "Lost";
    }

    @Override
    protected String getToolbarTitle() {
        return "Report Lost Item";
    }

    @Override
    protected String getSuccessMessage() {
        return "Lost item reported!";
    }
}
