package com.mobdeve.s17.grp2.archerfind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ManageItemAdapter extends RecyclerView.Adapter<ManageItemAdapter.ViewHolder> {

    private List<Item> items;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onEdit(Item item);
        void onResolve(Item item);
        void onDelete(Item item);
    }

    public ManageItemAdapter(List<Item> items, OnActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.status.setText(item.isResolved() ? "Resolved" : item.getStatus());
        holder.thumb.setImageResource(R.drawable.placeholder_image);

        holder.resolveButton.setEnabled(!item.isResolved());
        holder.resolveButton.setText(item.isResolved() ? "Resolved" : "Resolve");

        holder.editButton.setOnClickListener(v -> listener.onEdit(item));
        holder.resolveButton.setOnClickListener(v -> listener.onResolve(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status;
        ImageView thumb;
        com.google.android.material.button.MaterialButton editButton, resolveButton, deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_manage_title);
            status = itemView.findViewById(R.id.tv_manage_status);
            thumb = itemView.findViewById(R.id.iv_manage_thumb);
            editButton = itemView.findViewById(R.id.btn_edit);
            resolveButton = itemView.findViewById(R.id.btn_resolve);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
