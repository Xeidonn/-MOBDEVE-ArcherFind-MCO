package com.mobdeve.s17.grp2.archerfind;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Item {
    private String id;
    private String title;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private String category;
    private String status; // "Lost" or "Found"
    private String photoUrl;
    private String ownerId;
    private String ownerName;
    private boolean resolved;
    @ServerTimestamp
    private Date createdAt;

    // Required no-arg constructor for Firestore deserialization.
    public Item() {
    }

    public Item(String title, String description, String location, String category,
                String status, String ownerId, String ownerName) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.category = category;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.resolved = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Exclude
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Exclude
    public String getFormattedDate() {
        if (createdAt == null) return "";
        return new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(createdAt);
    }
}
