package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String text;
    private String revealPhotoUrl; // set when the poster reveals the unblurred photo in-chat
    @ServerTimestamp
    private Date createdAt;

    // Required no-arg constructor for Firestore deserialization.
    public Message() {
    }

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getRevealPhotoUrl() { return revealPhotoUrl; }
    public void setRevealPhotoUrl(String revealPhotoUrl) { this.revealPhotoUrl = revealPhotoUrl; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
