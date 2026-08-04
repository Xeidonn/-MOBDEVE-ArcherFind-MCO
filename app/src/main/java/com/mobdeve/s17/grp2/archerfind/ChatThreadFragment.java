package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ChatThreadFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();
    private final ChatRepository chatRepository = new ChatRepository();
    private final ItemRepository itemRepository = new ItemRepository();
    private ListenerRegistration messagesListener;
    private String threadId;

    private MaterialButton revealButton;
    private MaterialButton resolveButton;
    // Whether the signed-in user is even allowed to see the reveal button (owner + item has a photo).
    private boolean canRevealPhoto;
    // Whether a reveal message already exists in this thread's history — derived from the
    // messages themselves so the button stays hidden even after leaving and reopening the chat.
    private boolean photoAlreadyRevealed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_thread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        threadId = getArguments() != null ? getArguments().getString("threadId") : null;
        String itemTitle = getArguments() != null ? getArguments().getString("itemTitle", "Chat") : "Chat";

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_chat_thread);
        toolbar.setTitle(itemTitle);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        RecyclerView rv = view.findViewById(R.id.rv_messages);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null || threadId == null) return;

        MessageAdapter adapter = new MessageAdapter(new ArrayList<>(), user.getUid());
        rv.setAdapter(adapter);

        messagesListener = chatRepository.listenMessages(threadId, new FirestoreListCallback<Message>() {
            @Override
            public void onChanged(List<Message> messages) {
                if (!isAdded()) return;
                adapter.setItems(messages);
                if (!messages.isEmpty()) rv.scrollToPosition(messages.size() - 1);

                for (Message m : messages) {
                    if (m.getRevealPhotoUrl() != null) {
                        photoAlreadyRevealed = true;
                        break;
                    }
                }
                updateRevealButtonVisibility();

                // Viewing the thread is the "seen it" signal, so re-stamp on every update
                // (covers both opening the thread and receiving a message while still in it).
                chatRepository.markThreadRead(threadId, user.getUid(), new FirestoreVoidCallback() {
                    @Override
                    public void onSuccess() { /* best-effort */ }

                    @Override
                    public void onError(Exception e) { /* best-effort */ }
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load messages: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        TextInputEditText etMessage = view.findViewById(R.id.et_chat_message);
        MaterialButton btnSend = view.findViewById(R.id.btn_send_message);
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
            if (text.isEmpty()) return;
            etMessage.setText("");
            chatRepository.sendMessage(threadId, new Message(user.getUid(), text), new FirestoreVoidCallback() {
                @Override
                public void onSuccess() {
                    // Real-time listener above will reflect the new message.
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    Snackbar.make(view, "Failed to send: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        });

        revealButton = view.findViewById(R.id.btn_reveal_photo);
        resolveButton = view.findViewById(R.id.btn_mark_resolved);
        setupOwnerControls(view, user.getUid());
    }

    // Only the item's original poster gets the reveal-photo and mark-resolved controls, and
    // only once we know which item this thread is about (fetched via the thread doc).
    private void setupOwnerControls(View view, String currentUserId) {
        chatRepository.getThread(threadId, new FirestoreCallback<ChatThread>() {
            @Override
            public void onSuccess(ChatThread thread) {
                if (!isAdded() || thread.getItemId() == null) return;
                itemRepository.getItem(thread.getItemId(), new FirestoreCallback<Item>() {
                    @Override
                    public void onSuccess(Item item) {
                        if (!isAdded() || !currentUserId.equals(item.getOwnerId())) return;

                        canRevealPhoto = item.getPhotoUrl() != null;
                        updateRevealButtonVisibility();
                        revealButton.setOnClickListener(v -> revealPhoto(view, currentUserId, item));

                        resolveButton.setVisibility(item.isResolved() ? View.GONE : View.VISIBLE);
                        resolveButton.setOnClickListener(v -> markResolved(view, currentUserId, item));
                    }

                    @Override
                    public void onError(Exception e) { /* item may have been deleted since; just skip the controls */ }
                });
            }

            @Override
            public void onError(Exception e) { /* skip the controls if the thread lookup fails */ }
        });
    }

    private void updateRevealButtonVisibility() {
        if (revealButton == null) return;
        revealButton.setVisibility(canRevealPhoto && !photoAlreadyRevealed ? View.VISIBLE : View.GONE);
    }

    private void revealPhoto(View view, String currentUserId, Item item) {
        revealButton.setEnabled(false);
        Message message = new Message(currentUserId, "📷 Shared the original photo for verification");
        message.setRevealPhotoUrl(item.getPhotoUrl());
        chatRepository.sendMessage(threadId, message, new FirestoreVoidCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                photoAlreadyRevealed = true;
                updateRevealButtonVisibility();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                revealButton.setEnabled(true);
                Snackbar.make(view, "Failed to reveal photo: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // Lets the poster resolve the item from the chat itself, instead of going back to
    // Manage Listings, and lets the other party know it happened right in the conversation.
    private void markResolved(View view, String currentUserId, Item item) {
        resolveButton.setEnabled(false);
        itemRepository.setResolved(item.getId(), true, new FirestoreVoidCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                resolveButton.setVisibility(View.GONE);
                Snackbar.make(view, "Marked '" + item.getTitle() + "' as resolved.", Snackbar.LENGTH_SHORT).show();
                chatRepository.sendMessage(threadId, new Message(currentUserId, "✅ Marked this item as resolved."), new FirestoreVoidCallback() {
                    @Override
                    public void onSuccess() { /* best-effort */ }

                    @Override
                    public void onError(Exception e) { /* best-effort */ }
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                resolveButton.setEnabled(true);
                Snackbar.make(view, "Failed to mark resolved: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }
}
