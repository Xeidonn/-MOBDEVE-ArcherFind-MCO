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

public class SearchFilterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_search_results);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Show all items as search results; clicking navigates to detail
        ItemAdapter adapter = new ItemAdapter(DummyData.getAllItems(), item -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemTitle", item.getTitle());
            Navigation.findNavController(view).navigate(R.id.action_search_to_itemDetail, bundle);
        });
        rv.setAdapter(adapter);
    }
}
