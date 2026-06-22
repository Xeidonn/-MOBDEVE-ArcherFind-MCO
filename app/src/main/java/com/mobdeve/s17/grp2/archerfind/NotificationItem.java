package com.mobdeve.s17.grp2.archerfind;

public class NotificationItem {
    private String title;
    private String message;
    private String time;

    public NotificationItem(String title, String message, String time) {
        this.title = title;
        this.message = message;
        this.time = time;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
}
