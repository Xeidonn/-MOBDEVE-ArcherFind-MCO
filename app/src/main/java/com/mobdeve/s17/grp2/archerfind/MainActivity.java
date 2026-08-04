package com.mobdeve.s17.grp2.archerfind;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final ChatRepository chatRepository = new ChatRepository();
    private ListenerRegistration notificationBadgeListener;
    private ListenerRegistration chatBadgeListener;
    private String badgeListenerUid;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { /* no-op either way */ });

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire up the NavHostFragment to get the NavController
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Connect bottom nav to NavController so tab taps navigate automatically
        bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        requestNotificationPermissionIfNeeded();

        // Skip Login on relaunch if a session is already active.
        AuthRepository authRepository = new AuthRepository();
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            authRepository.saveFcmToken(currentUser.getUid());
            navController.navigate(R.id.action_login_to_home);
        }

        // Hide bottom nav on Login and Register screens; keep the unread badges in sync
        // with whichever account is currently signed in.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.loginFragment || id == R.id.registerFragment) {
                bottomNav.setVisibility(View.GONE);
                detachBadgeListeners();
                bottomNav.getOrCreateBadge(R.id.notificationsFragment).setVisible(false);
                bottomNav.getOrCreateBadge(R.id.chatListFragment).setVisible(false);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
                attachBadgeListenersIfNeeded();
            }
        });
    }

    // Live counts of unread notifications/chats for the bottom nav badges. No-op if
    // listeners are already attached for this same account.
    private void attachBadgeListenersIfNeeded() {
        FirebaseUser user = new AuthRepository().getCurrentUser();
        if (user == null || user.getUid().equals(badgeListenerUid)) return;
        detachBadgeListeners();
        badgeListenerUid = user.getUid();

        notificationBadgeListener = notificationRepository.listenForUser(user.getUid(), new FirestoreListCallback<NotificationItem>() {
            @Override
            public void onChanged(List<NotificationItem> notifications) {
                int unread = 0;
                for (NotificationItem n : notifications) {
                    if (!n.isRead()) unread++;
                }
                setBadgeCount(R.id.notificationsFragment, unread);
            }

            @Override
            public void onError(Exception e) { /* best-effort, leave badge as-is */ }
        });

        chatBadgeListener = chatRepository.listenThreadsForUser(user.getUid(), new FirestoreListCallback<ChatThread>() {
            @Override
            public void onChanged(List<ChatThread> threads) {
                int unread = 0;
                for (ChatThread t : threads) {
                    if (t.isUnreadFor(user.getUid())) unread++;
                }
                setBadgeCount(R.id.chatListFragment, unread);
            }

            @Override
            public void onError(Exception e) { /* best-effort, leave badge as-is */ }
        });
    }

    private void setBadgeCount(int menuItemId, int count) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setVisible(count > 0);
        if (count > 0) badge.setNumber(count);
    }

    private void detachBadgeListeners() {
        if (notificationBadgeListener != null) {
            notificationBadgeListener.remove();
            notificationBadgeListener = null;
        }
        if (chatBadgeListener != null) {
            chatBadgeListener.remove();
            chatBadgeListener = null;
        }
        badgeListenerUid = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachBadgeListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
