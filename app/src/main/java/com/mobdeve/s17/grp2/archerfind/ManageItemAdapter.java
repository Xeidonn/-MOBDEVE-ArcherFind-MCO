package com.mobdeve.s17.grp2.archerfind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class ManageItemAdapter extends RecyclerView.Adapter<ManageItemAdapter.ViewHolder> {

    private final List<Item> items;

    public ManageItemAdapter(List<Item> items) {
        this.items = items;
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
        holder.status.setText(item.getStatus());
        holder.thumb.setImageResource(item.getImageResId());

        // UI-only feedback via Snackbar
        holder.itemView.findViewById(R.id.btn_edit).setOnClickListener(v ->
                Snackbar.make(v, "Edit: " + item.getTitle(), Snackbar.LENGTH_SHORT).show());

        holder.itemView.findViewById(R.id.btn_resolve).setOnClickListener(v ->
                Snackbar.make(v, "Marked as resolved: " + item.getTitle(), Snackbar.LENGTH_SHORT).show());

        holder.itemView.findViewById(R.id.btn_delete).setOnClickListener(v ->
                Snackbar.make(v, "Deleted: " + item.getTitle(), Snackbar.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status;
        ImageView thumb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_manage_title);
            status = itemView.findViewById(R.id.tv_manage_status);
            thumb = itemView.findViewById(R.id.iv_manage_thumb);
        }
    }
}
