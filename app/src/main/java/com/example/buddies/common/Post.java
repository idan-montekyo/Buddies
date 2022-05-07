package com.example.buddies.common;

import android.icu.text.SimpleDateFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

public class Post {

    String creatorUserUID;
    String meetingCity;
    String meetingStreet;
    String meetingTime;
    LatLng meetingLocation;
    String postContent;
    // TODO: add postCreationDate. (LocalDate won't work with FireBase)
    LocalTime postCreationTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Post(String creatorUserUID, String meetingCity, String meetingStreet,
                String meetingTime, LatLng meetingLocation, String postContent) {

        this.creatorUserUID = creatorUserUID;
        this.meetingCity = meetingCity;
        this.meetingStreet = meetingStreet;
        this.meetingTime = meetingTime;
        this.meetingLocation = meetingLocation;
        this.postContent = postContent;

        // TODO: add postCreationDate. (LocalDate won't work with FireBase)
        this.postCreationTime = LocalTime.now();
    }

    // Getters
    public String getCreatorUserUID() { return creatorUserUID; }
    public String getMeetingCity() { return meetingCity; }
    public String getMeetingStreet() { return meetingStreet; }
    public String getMeetingTime() { return meetingTime; }
    public LatLng getMeetingLocation() { return meetingLocation; }
    public String getPostContent() { return postContent; }
    public LocalTime getPostCreationTime() { return postCreationTime; }

    // Setters
    public void setMeetingCity(String meetingCity) { this.meetingCity = meetingCity; }
    public void setMeetingStreet(String meetingStreet) { this.meetingStreet = meetingStreet; }
    public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }
    public void setMeetingLocation(LatLng meetingLocation) { this.meetingLocation = meetingLocation; }
    public void setPostContent(String postContent) { this.postContent = postContent; }
}
