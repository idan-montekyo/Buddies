package com.example.buddies.common;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalTime;

public class Post implements Comparable<Post> {

    private String creatorUserUID;
    private String meetingCity;
    private String meetingStreet;
    private String meetingTime;
    private LatLng meetingLocation;
    private String postContent;
    private LocalTime postCreationTime;
    private CreationDate postCreationDate;
    private long postCreationDateTimeAsLong;
    private String postID = null;

    public Post() { }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Post(String i_CreatorUserUID, String i_MeetingCity, String i_MeetingStreet,
                String i_MeetingTime, LatLng i_MeetingLocation, String i_PostContent)
    {

        this.creatorUserUID = i_CreatorUserUID;
        this.meetingCity = i_MeetingCity;
        this.meetingStreet = i_MeetingStreet;
        this.meetingTime = i_MeetingTime;
        this.meetingLocation = i_MeetingLocation;
        this.postContent = i_PostContent;

        // Gets current time based on user's current location.
        this.postCreationTime = LocalTime.now();

        // Initialize date holder class.
        this.postCreationDate = CreationDate.now();

        // Creating a long representing YYYY-MM-DD-HH-MM-SS for more convenient comparisons.
        // Meaning - newer posts will have higher value, and older posts will have lower value.
        String yearAsString = String.valueOf(this.postCreationDate.getCreationYear());
        String monthAsString = this.postCreationDate.getCreationMonth() > 10 ?
                String.valueOf(this.postCreationDate.getCreationMonth()) : "0" + String.valueOf(this.postCreationDate.getCreationMonth());
        String dayAsString = this.postCreationDate.getCreationDay() > 10 ?
                String.valueOf(this.postCreationDate.getCreationDay()) : "0" + String.valueOf(this.postCreationDate.getCreationDay());
        String hourAsString = this.postCreationTime.getHour() > 10 ?
                String.valueOf(this.postCreationTime.getHour()) :
                "0" + String.valueOf(this.postCreationTime.getHour());
        String minuteAsString = this.postCreationTime.getMinute() > 10 ?
                String.valueOf(this.postCreationTime.getMinute()) :
                "0" + String.valueOf(this.postCreationTime.getMinute());
        String secondAsString = this.postCreationTime.getSecond() > 10 ?
                String.valueOf(this.postCreationTime.getSecond()) :
                "0" + String.valueOf(this.postCreationTime.getSecond());
        this.postCreationDateTimeAsLong = Long.parseLong(yearAsString + monthAsString + dayAsString
                                                         + hourAsString + minuteAsString + secondAsString);
    }

    // Constructor for existing posts, loaded from FireBase.
    public Post(String i_CreatorUserUID, String i_MeetingCity, String i_MeetingStreet,
                String i_MeetingTime, LatLng i_MeetingLocation, String i_PostContent,
                LocalTime i_PostCreationTime, long i_PostCreationDateTimeAsLong,
                int i_PostCreationYear, int i_PostCreationMonth, int i_PostCreationDay)
    {
        this.creatorUserUID = i_CreatorUserUID;
        this.meetingCity = i_MeetingCity;
        this.meetingStreet = i_MeetingStreet;
        this.meetingTime = i_MeetingTime;
        this.meetingLocation = i_MeetingLocation;
        this.postContent = i_PostContent;
        this.postCreationTime = i_PostCreationTime;
        // Initialize date holder class.
        this.postCreationDate = new CreationDate(i_PostCreationYear, i_PostCreationMonth,
                                                   i_PostCreationDay);
        this.postCreationDateTimeAsLong = i_PostCreationDateTimeAsLong;
    }

    // Getters
    public String getCreatorUserUID() { return creatorUserUID; }
    public String getMeetingCity() { return meetingCity; }
    public String getMeetingStreet() { return meetingStreet; }
    public String getMeetingTime() { return meetingTime; }
    public LatLng getMeetingLocation() { return meetingLocation; }
    public String getPostContent() { return postContent; }
    public LocalTime getPostCreationTime() { return postCreationTime; }
    public CreationDate getPostCreationDate() { return postCreationDate; }
    public long getPostCreationDateTimeAsLong() { return postCreationDateTimeAsLong; }
    public String getPostID() { return this.postID; }

    // Setters
    public void setMeetingCity(String meetingCity) { this.meetingCity = meetingCity; }
    public void setMeetingStreet(String meetingStreet) { this.meetingStreet = meetingStreet; }
    public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }
    public void setMeetingLocation(LatLng meetingLocation) { this.meetingLocation = meetingLocation; }
    public void setPostContent(String postContent) { this.postContent = postContent; }
    public void setPostID(String postID) { this.postID = postID; }

    @Override
    public int compareTo(Post other) {
        long comparison = other.getPostCreationDateTimeAsLong() - this.getPostCreationDateTimeAsLong();
        return (int)comparison;
    }

    @NonNull
    @Override
    public String toString() {
        return "Post{" +
                "creatorUserUID='" + creatorUserUID + '\'' +
                ", meetingCity='" + meetingCity + '\'' +
                ", meetingStreet='" + meetingStreet + '\'' +
                ", meetingTime='" + meetingTime + '\'' +
                ", meetingLocation=" + meetingLocation +
                ", postContent='" + postContent + '\'' +
                ", postCreationTime=" + postCreationTime +
                ", postCreationDate=" + postCreationDate +
                ", postCreationDateTimeAsLong=" + postCreationDateTimeAsLong +
                '}';
    }
}
