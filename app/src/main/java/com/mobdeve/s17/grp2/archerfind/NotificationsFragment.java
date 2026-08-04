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

public class NotificationsFragment extends Fragment {

    private final AuthRepository authRepository = new AuthRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private NotificationAdapter adapter;
    private ListenerRegistration notificationsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            Bundle bundle = new Bundle();
            if (notification.getRelatedChatId() != null) {
                bundle.putString("threadId", notification.getRelatedChatId());
                Navigation.findNavController(view).navigate(R.id.action_notifications_to_chatThread, bundle);
            } else if (notification.getRelatedItemId() != null) {
                bundle.putString("itemId", notification.getRelatedItemId());
                Navigation.findNavController(view).navigate(R.id.action_notifications_to_itemDetail, bundle);
            } else {
                Snackbar.make(view, "Nothing to open for this notification.", Snackbar.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        TextView emptyState = view.findViewById(R.id.tv_notifications_empty);

        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) return;

        notificationsListener = notificationRepository.listenForUser(user.getUid(), new FirestoreListCallback<NotificationItem>() {
            @Override
            public void onChanged(List<NotificationItem> notifications) {
                if (!isAdded()) return;
                adapter.setItems(notifications);
                emptyState.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load notifications: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        // Opening this tab is the "seen it" signal — clears the unread badge on the bottom nav.
        notificationRepository.markAllRead(user.getUid(), new FirestoreVoidCallback() {
            @Override
            public void onSuccess() { /* best-effort */ }

            @Override
            public void onError(Exception e) { /* best-effort */ }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }
}
