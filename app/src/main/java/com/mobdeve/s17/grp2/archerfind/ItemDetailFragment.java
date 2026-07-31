package com.mobdeve.s17.grp2.archerfind;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

public class ItemDetailFragment extends Fragment {

    private final ItemRepository itemRepository = new ItemRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final ChatRepository chatRepository = new ChatRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final CommentRepository commentRepository = new CommentRepository();
    private ListenerRegistration commentsListener;
    private Item currentItem;

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
        claimButton.setOnClickListener(v -> onClaimClicked(view, claimButton));

        String itemId = getArguments() != null ? getArguments().getString("itemId") : null;
        if (itemId == null) {
            Snackbar.make(view, "Item not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        setupComments(view, itemId);

        itemRepository.getItem(itemId, new FirestoreCallback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (!isAdded()) return;
                currentItem = item;
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

        MaterialButton mapButton = view.findViewById(R.id.btn_view_on_map);
        if (item.hasLocation()) {
            mapButton.setVisibility(View.VISIBLE);
            mapButton.setOnClickListener(v -> {
                String uri = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                        item.getLatitude(), item.getLongitude(), item.getLatitude(), item.getLongitude(), item.getTitle());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Snackbar.make(view, "No map app available to open this location.", Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            mapButton.setVisibility(View.GONE);
        }
    }

    private void setupComments(View view, String itemId) {
        RecyclerView rv = view.findViewById(R.id.rv_comments);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        CommentAdapter adapter = new CommentAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        TextView emptyState = view.findViewById(R.id.tv_comments_empty);

        commentsListener = commentRepository.listenForItem(itemId, new FirestoreListCallback<Comment>() {
            @Override
            public void onChanged(List<Comment> comments) {
                if (!isAdded()) return;
                adapter.setItems(comments);
                emptyState.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load comments: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        TextInputEditText etComment = view.findViewById(R.id.et_comment);
        view.findViewById(R.id.btn_post_comment).setOnClickListener(v -> {
            String text = etComment.getText() != null ? etComment.getText().toString().trim() : "";
            if (text.isEmpty()) return;

            FirebaseUser user = authRepository.getCurrentUser();
            if (user == null) return;

            String authorName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();
            etComment.setText("");
            commentRepository.addComment(new Comment(itemId, user.getUid(), authorName, text), new FirestoreVoidCallback() {
                @Override
                public void onSuccess() {
                    if (!isAdded() || currentItem == null || currentItem.getOwnerId().equals(user.getUid())) return;
                    NotificationItem notification = new NotificationItem(currentItem.getOwnerId(),
                            NotificationItem.TYPE_COMMENT, "New Comment",
                            authorName + " commented on your '" + currentItem.getTitle() + "' posting.");
                    notification.setRelatedItemId(itemId);
                    notificationRepository.create(notification, new FirestoreVoidCallback() {
                        @Override
                        public void onSuccess() { /* best-effort */ }

                        @Override
                        public void onError(Exception e) { /* best-effort */ }
                    });
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    Snackbar.make(view, "Failed to post comment: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
    }

    private void onClaimClicked(View view, MaterialButton claimButton) {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null || currentItem == null) return;

        claimButton.setEnabled(false);
        chatRepository.getOrCreateThread(currentItem.getId(), currentItem.getTitle(),
                currentItem.getOwnerId(), user.getUid(), new FirestoreCallback<ChatThread>() {
            @Override
            public void onSuccess(ChatThread thread) {
                if (!isAdded()) return;
                claimButton.setEnabled(true);

                NotificationItem notification = new NotificationItem(currentItem.getOwnerId(),
                        NotificationItem.TYPE_CLAIM, "Item Claimed",
                        "Someone is interested in your '" + currentItem.getTitle() + "' posting.");
                notification.setRelatedChatId(thread.getId());
                notification.setRelatedItemId(currentItem.getId());
                notificationRepository.create(notification, new FirestoreVoidCallback() {
                    @Override
                    public void onSuccess() { /* best-effort, nothing to do */ }

                    @Override
                    public void onError(Exception e) { /* best-effort, don't block the chat navigation */ }
                });

                Bundle bundle = new Bundle();
                bundle.putString("threadId", thread.getId());
                bundle.putString("itemTitle", currentItem.getTitle());
                Navigation.findNavController(view).navigate(R.id.action_itemDetail_to_chatThread, bundle);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                claimButton.setEnabled(true);
                Snackbar.make(view, "Failed to start chat: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
