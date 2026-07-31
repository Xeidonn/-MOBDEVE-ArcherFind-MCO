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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ManageListingsFragment extends Fragment {

    private final ItemRepository itemRepository = new ItemRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private ManageItemAdapter adapter;
    private ListenerRegistration itemsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.toolbar_manage).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        RecyclerView rv = view.findViewById(R.id.rv_my_listings);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ManageItemAdapter(new ArrayList<>(), new ManageItemAdapter.OnActionListener() {
            @Override
            public void onEdit(Item item) {
                // Editing is not implemented yet; there is no dedicated edit screen.
                Snackbar.make(view, "Editing isn't available yet.", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onResolve(Item item) {
                itemRepository.setResolved(item.getId(), true, new FirestoreVoidCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;
                        Snackbar.make(view, "Marked as resolved: " + item.getTitle(), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        Snackbar.make(view, "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onDelete(Item item) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete listing?")
                        .setMessage("This can't be undone.")
                        .setPositiveButton("Delete", (dialog, which) ->
                                itemRepository.deleteItem(item.getId(), new FirestoreVoidCallback() {
                                    @Override
                                    public void onSuccess() {
                                        if (!isAdded()) return;
                                        Snackbar.make(view, "Deleted: " + item.getTitle(), Snackbar.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        if (!isAdded()) return;
                                        Snackbar.make(view, "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                }))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            itemsListener = itemRepository.listenItemsByOwner(user.getUid(), new FirestoreListCallback<Item>() {
                @Override
                public void onChanged(List<Item> items) {
                    if (!isAdded()) return;
                    adapter.setItems(items);
                    view.findViewById(R.id.tv_manage_empty).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    if (!isAdded()) return;
                    Snackbar.make(view, "Failed to load listings: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
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
