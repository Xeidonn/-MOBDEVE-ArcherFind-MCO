package com.mobdeve.s17.grp2.archerfind;

public class Item {
    private String title;
    private String description;
    private String location;
    private String date;
    private String status; // "Lost" or "Found"
    private int imageResId;

    public Item(String title, String description, String location, String date, String status, int imageResId) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.status = status;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public int getImageResId() { return imageResId; }
}
