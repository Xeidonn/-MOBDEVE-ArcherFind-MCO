package com.mobdeve.s17.grp2.archerfind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(List<Item> items, OnItemClickListener listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.location.setText(item.getLocation());
        holder.date.setText(item.getFormattedDate());
        holder.status.setText(item.getStatus());
        Glide.with(holder.thumb.getContext())
                .load(item.getPhotoUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.thumb);

        if (item.getStatus().equals("Lost")) {
            holder.status.setBackgroundResource(R.color.badge_lost);
        } else {
            holder.status.setBackgroundResource(R.color.badge_found);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, date, status;
        ImageView thumb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_item_title);
            location = itemView.findViewById(R.id.tv_item_location);
            date = itemView.findViewById(R.id.tv_item_date);
            status = itemView.findViewById(R.id.tv_item_status);
            thumb = itemView.findViewById(R.id.iv_item_thumb);
        }
    }
}
