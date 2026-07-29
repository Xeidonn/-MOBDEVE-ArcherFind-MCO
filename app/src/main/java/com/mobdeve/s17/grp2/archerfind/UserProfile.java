package com.mobdeve.s17.grp2.archerfind;

public class UserProfile {
    private String uid;
    private String fullName;
    private String email;
    private String studentId;
    private String profilePhotoUrl;

    // Required no-arg constructor for Firestore deserialization.
    public UserProfile() {
    }

    public UserProfile(String uid, String fullName, String email, String studentId) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
}
