package com.mobdeve.s17.grp2.archerfind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatThreadAdapter extends RecyclerView.Adapter<ChatThreadAdapter.ViewHolder> {

    private List<ChatThread> threads;
    private final OnThreadClickListener listener;

    public interface OnThreadClickListener {
        void onThreadClick(ChatThread thread);
    }

    public ChatThreadAdapter(List<ChatThread> threads, OnThreadClickListener listener) {
        this.threads = threads;
        this.listener = listener;
    }

    public void setItems(List<ChatThread> threads) {
        this.threads = threads;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_thread_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatThread thread = threads.get(position);
        holder.itemTitle.setText("Re: " + thread.getItemTitle());
        holder.lastMessage.setText(thread.getLastMessage() == null || thread.getLastMessage().isEmpty()
                ? "No messages yet" : thread.getLastMessage());
        holder.time.setText(thread.getRelativeLastMessageTime());
        holder.itemView.setOnClickListener(v -> listener.onThreadClick(thread));
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, lastMessage, time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.tv_thread_item_title);
            lastMessage = itemView.findViewById(R.id.tv_thread_last_message);
            time = itemView.findViewById(R.id.tv_thread_time);
        }
    }
}
