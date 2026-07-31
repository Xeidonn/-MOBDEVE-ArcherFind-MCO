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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private final ItemRepository itemRepository = new ItemRepository();
    private ItemAdapter adapter;
    private ListenerRegistration itemsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_items);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(new ArrayList<>(), item -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemId", item.getId());
            Navigation.findNavController(view).navigate(R.id.action_home_to_itemDetail, bundle);
        });
        rv.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        subscribeToStatus(view, "Lost");
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                subscribeToStatus(view, tab.getPosition() == 0 ? "Lost" : "Found");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_post);
        fab.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("What would you like to post?")
                .setItems(new CharSequence[]{"Lost Item", "Found Item"}, (dialog, which) -> {
                    int action = which == 0 ? R.id.action_home_to_postLost : R.id.action_home_to_postFound;
                    Navigation.findNavController(v).navigate(action);
                })
                .show());
    }

    private void subscribeToStatus(View view, String status) {
        if (itemsListener != null) itemsListener.remove();
        itemsListener = itemRepository.listenItemsByStatus(status, new FirestoreListCallback<Item>() {
            @Override
            public void onChanged(List<Item> items) {
                if (!isAdded()) return;
                adapter.setItems(items);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Failed to load items: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (itemsListener != null) {
            itemsListener.remove();
            itemsListener = null;
        }
    }
}
