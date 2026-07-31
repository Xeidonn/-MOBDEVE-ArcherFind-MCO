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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();
    private final ChatRepository chatRepository = new ChatRepository();
    private ListenerRegistration threadsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_chat_threads);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        TextView emptyState = view.findViewById(R.id.tv_chat_list_empty);

        ChatThreadAdapter adapter = new ChatThreadAdapter(new ArrayList<>(), thread -> {
            Bundle bundle = new Bundle();
            bundle.putString("threadId", thread.getId());
            bundle.putString("itemTitle", thread.getItemTitle());
            Navigation.findNavController(view).navigate(R.id.action_chatList_to_chatThread, bundle);
        });
        rv.setAdapter(adapter);

        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) return;

        threadsListener = chatRepository.listenThreadsForUser(user.getUid(), new FirestoreListCallback<ChatThread>() {
            @Override
            public void onChanged(List<ChatThread> threads) {
                if (!isAdded()) return;
                adapter.setItems(threads);
                emptyState.setVisibility(threads.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load chats: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (threadsListener != null) {
            threadsListener.remove();
            threadsListener = null;
        }
    }
}
