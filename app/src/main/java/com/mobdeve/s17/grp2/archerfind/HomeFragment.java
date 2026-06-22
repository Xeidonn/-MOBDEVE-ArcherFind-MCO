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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    private ItemAdapter adapter;

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

        // Clicking an item navigates to ItemDetailFragment
        adapter = new ItemAdapter(DummyData.getAllItems(), item -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemTitle", item.getTitle());
            Navigation.findNavController(view).navigate(R.id.action_home_to_itemDetail, bundle);
        });
        rv.setAdapter(adapter);

        // Tab switching between Lost / Found / All
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.setItems(DummyData.getLostItems());
                } else {
                    adapter.setItems(DummyData.getFoundItems());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // FAB opens Post Lost Item by default
        FloatingActionButton fab = view.findViewById(R.id.fab_post);
        fab.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_postLost));
    }
}
