package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ChatThread {
    private String id;
    private List<String> participantIds;
    private String itemId;
    private String itemTitle;
    private String lastMessage;
    private String lastMessageSenderId;
    @ServerTimestamp
    private Date lastMessageAt;
    // Per-participant "last opened this thread" timestamp, keyed by uid; used to derive
    // unread state instead of a plain boolean, since a thread has more than one reader.
    private Map<String, Date> lastReadAt = new HashMap<>();

    // Required no-arg constructor for Firestore deserialization.
    public ChatThread() {
        this.participantIds = new ArrayList<>();
    }

    public ChatThread(String posterId, String claimantId, String itemId, String itemTitle) {
        this.participantIds = new ArrayList<>();
        this.participantIds.add(posterId);
        this.participantIds.add(claimantId);
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.lastMessage = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Date getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Date lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public Map<String, Date> getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Map<String, Date> lastReadAt) { this.lastReadAt = lastReadAt; }

    // A thread is unread for a user if someone else sent the last message and this user
    // hasn't opened the thread since then (or never has).
    @Exclude
    public boolean isUnreadFor(String userId) {
        if (lastMessageAt == null || userId.equals(lastMessageSenderId)) return false;
        Date readAt = lastReadAt != null ? lastReadAt.get(userId) : null;
        return readAt == null || readAt.before(lastMessageAt);
    }

    @Exclude
    public String getOtherParticipantId(String currentUserId) {
        if (participantIds == null) return null;
        for (String id : participantIds) {
            if (!id.equals(currentUserId)) return id;
        }
        return null;
    }

    @Exclude
    public String getRelativeLastMessageTime() {
        if (lastMessageAt == null) return "";
        long diffMs = System.currentTimeMillis() - lastMessageAt.getTime();
        long minutes = diffMs / (60 * 1000);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }
}
