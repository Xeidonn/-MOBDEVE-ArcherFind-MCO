package com.mobdeve.s17.grp2.archerfind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private List<Message> messages;
    private final String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setItems(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_bubble, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean isMine = currentUserId.equals(message.getSenderId());

        holder.text.setText(message.getText());
        holder.time.setText(message.getCreatedAt() != null ? timeFormat.format(message.getCreatedAt()) : "");

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.card.getLayoutParams();
        params.gravity = isMine ? android.view.Gravity.END : android.view.Gravity.START;
        holder.card.setLayoutParams(params);

        int bg = isMine ? R.color.green_primary : R.color.light_gray;
        int fg = isMine ? R.color.white : R.color.dark_gray;
        holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), bg));
        holder.text.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fg));
        holder.time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fg));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView text, time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_bubble);
            text = itemView.findViewById(R.id.tv_message_text);
            time = itemView.findViewById(R.id.tv_message_time);
        }
    }
}
