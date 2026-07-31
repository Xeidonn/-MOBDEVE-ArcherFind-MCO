package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

public class ItemDetailFragment extends Fragment {

    private final ItemRepository itemRepository = new ItemRepository();
    private final AuthRepository authRepository = new AuthRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.toolbar_detail).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        MaterialButton claimButton = view.findViewById(R.id.btn_claim);
        claimButton.setOnClickListener(v ->
                Snackbar.make(v, "Claim request submitted!", Snackbar.LENGTH_SHORT).show());

        String itemId = getArguments() != null ? getArguments().getString("itemId") : null;
        if (itemId == null) {
            Snackbar.make(view, "Item not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        itemRepository.getItem(itemId, new FirestoreCallback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (!isAdded()) return;
                bindItem(view, item, claimButton);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load item: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void bindItem(View view, Item item, MaterialButton claimButton) {
        ImageView photo = view.findViewById(R.id.iv_detail_photo);
        Glide.with(photo.getContext())
                .load(item.getPhotoUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(photo);

        ((TextView) view.findViewById(R.id.tv_detail_title)).setText(item.getTitle());
        ((TextView) view.findViewById(R.id.tv_detail_description)).setText(item.getDescription());
        ((TextView) view.findViewById(R.id.tv_detail_location)).setText("📍 " + item.getLocation());
        ((TextView) view.findViewById(R.id.tv_detail_date)).setText("📅 " + item.getFormattedDate());

        TextView statusView = view.findViewById(R.id.tv_detail_status);
        if (item.isResolved()) {
            statusView.setText("Resolved");
            statusView.setBackgroundResource(R.color.badge_found);
        } else {
            statusView.setText(item.getStatus());
            statusView.setBackgroundResource(
                    "Lost".equals(item.getStatus()) ? R.color.badge_lost : R.color.badge_found);
        }

        FirebaseUser currentUser = authRepository.getCurrentUser();
        boolean isOwner = currentUser != null && currentUser.getUid().equals(item.getOwnerId());
        claimButton.setVisibility(isOwner || item.isResolved() ? View.GONE : View.VISIBLE);
    }
}
