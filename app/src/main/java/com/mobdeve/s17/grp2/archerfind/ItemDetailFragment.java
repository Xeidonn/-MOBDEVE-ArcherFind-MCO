package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;

public class ItemDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Display the item title passed via navigation args
        if (getArguments() != null) {
            String title = getArguments().getString("itemTitle", "Lost Item");
            ((TextView) view.findViewById(R.id.tv_detail_title)).setText(title);
        }

        // Toolbar back button
        view.findViewById(R.id.toolbar_detail).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        // Claim button shows Snackbar feedback (UI only)
        view.findViewById(R.id.btn_claim).setOnClickListener(v ->
                Snackbar.make(v, "Claim request submitted!", Snackbar.LENGTH_SHORT).show());
    }
}
