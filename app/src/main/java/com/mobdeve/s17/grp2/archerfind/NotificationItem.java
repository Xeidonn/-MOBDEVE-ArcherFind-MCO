package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationItem {

    public static final String TYPE_CLAIM = "CLAIM";
    public static final String TYPE_COMMENT = "COMMENT";
    public static final String TYPE_MESSAGE = "MESSAGE";
    public static final String TYPE_SYSTEM = "SYSTEM";

    private String id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private String relatedItemId;
    private String relatedChatId;
    @ServerTimestamp
    private Date createdAt;

    // Required no-arg constructor for Firestore deserialization.
    public NotificationItem() {
    }

    public NotificationItem(String userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getRelatedItemId() { return relatedItemId; }
    public void setRelatedItemId(String relatedItemId) { this.relatedItemId = relatedItemId; }

    public String getRelatedChatId() { return relatedChatId; }
    public void setRelatedChatId(String relatedChatId) { this.relatedChatId = relatedChatId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Exclude
    public String getRelativeTime() {
        if (createdAt == null) return "";
        long diffMs = System.currentTimeMillis() - createdAt.getTime();
        long minutes = diffMs / (60 * 1000);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        return days + (days == 1 ? " day ago" : " days ago");
    }
}
