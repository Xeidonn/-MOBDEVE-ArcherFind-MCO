package com.mobdeve.s17.grp2.archerfind;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchFilterFragment extends Fragment {

    private final ItemRepository itemRepository = new ItemRepository();
    private ItemAdapter adapter;

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

        adapter = new ItemAdapter(new ArrayList<>(), item -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemId", item.getId());
            Navigation.findNavController(view).navigate(R.id.action_search_to_itemDetail, bundle);
        });
        rv.setAdapter(adapter);

        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filters);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                runSearch(view, etSearch, chipGroup);
            }
        });
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> runSearch(view, etSearch, chipGroup));

        runSearch(view, etSearch, chipGroup);
    }

    private void runSearch(View view, TextInputEditText etSearch, ChipGroup chipGroup) {
        String query = etSearch.getText() != null ? etSearch.getText().toString() : "";
        String status = null;
        String category = null;
        boolean todayOnly = false;

        for (int id : chipGroup.getCheckedChipIds()) {
            if (id == R.id.chip_lost) status = "Lost";
            else if (id == R.id.chip_found) status = "Found";
            else if (id == R.id.chip_electronics) category = "Electronics";
            else if (id == R.id.chip_personal) category = "Personal";
            else if (id == R.id.chip_today) todayOnly = true;
        }

        boolean finalTodayOnly = todayOnly;
        itemRepository.searchItems(query, status, category, new FirestoreListCallback<Item>() {
            @Override
            public void onChanged(List<Item> items) {
                if (!isAdded()) return;
                List<Item> results = finalTodayOnly ? filterToday(items) : items;
                adapter.setItems(results);
                view.findViewById(R.id.tv_search_empty).setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Snackbar.make(view, "Search failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private List<Item> filterToday(List<Item> items) {
        List<Item> filtered = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        for (Item item : items) {
            if (item.getCreatedAt() == null) continue;
            Calendar itemDay = Calendar.getInstance();
            itemDay.setTime(item.getCreatedAt());
            if (itemDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && itemDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
