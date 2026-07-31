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

        setupRevealPhotoButton(view, user.getUid());
    }

    // Only the item's original poster can reveal the unblurred photo, and only
    // once we know which item this thread is about (fetched via the thread doc).
    private void setupRevealPhotoButton(View view, String currentUserId) {
        MaterialButton revealButton = view.findViewById(R.id.btn_reveal_photo);
        chatRepository.getThread(threadId, new FirestoreCallback<ChatThread>() {
            @Override
            public void onSuccess(ChatThread thread) {
                if (!isAdded() || thread.getItemId() == null) return;
                itemRepository.getItem(thread.getItemId(), new FirestoreCallback<Item>() {
                    @Override
                    public void onSuccess(Item item) {
                        if (!isAdded() || !currentUserId.equals(item.getOwnerId()) || item.getPhotoUrl() == null) return;
                        revealButton.setVisibility(View.VISIBLE);
                        revealButton.setOnClickListener(v -> {
                            revealButton.setEnabled(false);
                            Message message = new Message(currentUserId, "📷 Shared the original photo for verification");
                            message.setRevealPhotoUrl(item.getPhotoUrl());
                            chatRepository.sendMessage(threadId, message, new FirestoreVoidCallback() {
                                @Override
                                public void onSuccess() {
                                    if (isAdded()) revealButton.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    if (!isAdded()) return;
                                    revealButton.setEnabled(true);
                                    Snackbar.make(view, "Failed to reveal photo: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        });
                    }

                    @Override
                    public void onError(Exception e) { /* item may have been deleted since; just skip the button */ }
                });
            }

            @Override
            public void onError(Exception e) { /* skip the button if the thread lookup fails */ }
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
