package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comment {
    private String id;
    private String itemId;
    private String authorId;
    private String authorName;
    private String text;
    @ServerTimestamp
    private Date createdAt;

    // Required no-arg constructor for Firestore deserialization.
    public Comment() {
    }

    public Comment(String itemId, String authorId, String authorName, String text) {
        this.itemId = itemId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
