package com.example.buddies.common;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;

public class Comment implements Comparable<Comment>
{
    private String creatorUserUID;
    private String userProfileImageUri = null;
    private String commentContent;
    private LocalTime commentCreationTime;
    private CreationDate commentCreationDate;
    private long commentCreationDateTimeAsLong;
    private String commentID = null;
    private String ownerPostID = null;

    public Comment() { }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Comment(String i_CreatorUserUID, String i_UserProfileImageUri, String i_CommentContent, String i_OwnerPostID)
    {
        this.creatorUserUID = i_CreatorUserUID;
        this.userProfileImageUri = i_UserProfileImageUri;
        this.commentContent = i_CommentContent;

        // Gets current time based on user's current location.
        this.commentCreationTime = LocalTime.now();

        /*
        ZoneId zoneId = ZoneId.of("Israel");
        int year = (Year.now(zoneId)).getValue();
        int month = (YearMonth.now(zoneId)).getMonthValue();
        int day = (MonthDay.now(zoneId)).getDayOfMonth();

        this.commentCreationDate = new CreationDate(year, month, day);
         */

        // Initialize date holder class.
        this.commentCreationDate = CreationDate.now();

        // Creating a long representing YYYY-MM-DD-HH-MM-SS for more convenient comparisons.
        // Meaning - newer posts will have higher value, and older posts will have lower value.
        String yearAsString = String.valueOf(this.commentCreationDate.getCreationYear());
        String monthAsString = this.commentCreationDate.getCreationMonth() > 10 ?
                String.valueOf(this.commentCreationDate.getCreationMonth()) : "0" + String.valueOf(this.commentCreationDate.getCreationMonth());
        String dayAsString = this.commentCreationDate.getCreationDay() > 10 ?
                String.valueOf(this.commentCreationDate.getCreationDay()) : "0" + String.valueOf(this.commentCreationDate.getCreationDay());
        String hourAsString = this.commentCreationTime.getHour() > 10 ?
                String.valueOf(this.commentCreationTime.getHour()) :
                "0" + String.valueOf(this.commentCreationTime.getHour());
        String minuteAsString = this.commentCreationTime.getMinute() > 10 ?
                String.valueOf(this.commentCreationTime.getMinute()) :
                "0" + String.valueOf(this.commentCreationTime.getMinute());
        String secondAsString = this.commentCreationTime.getSecond() > 10 ?
                String.valueOf(this.commentCreationTime.getSecond()) :
                "0" + String.valueOf(this.commentCreationTime.getSecond());
        this.commentCreationDateTimeAsLong = Long.parseLong(yearAsString + monthAsString + dayAsString
                + hourAsString + minuteAsString + secondAsString);

        this.ownerPostID = i_OwnerPostID;
    }

    // Constructor for existing posts, loaded from FireBase.
    public Comment(String i_CreatorUserUID, String i_UserProfileImageUri, String i_CommentContent,
                   LocalTime i_CommentCreationTime, long i_CommentCreationDateTimeAsLong,
                   int i_CommentCreationYear, int i_CommentCreationMonth, int i_CommentCreationDay,
                   String i_OwnerPostID, String i_CommentID)
    {

        this.creatorUserUID = i_CreatorUserUID;
        this.userProfileImageUri = i_UserProfileImageUri;
        this.commentContent = i_CommentContent;
        this.commentCreationTime = i_CommentCreationTime;

        // Initialize date holder class.
        this.commentCreationDate = new CreationDate(i_CommentCreationYear, i_CommentCreationMonth,
                                                      i_CommentCreationDay);
        this.commentCreationDateTimeAsLong = i_CommentCreationDateTimeAsLong;

        this.ownerPostID = i_OwnerPostID;
        this.commentID = i_CommentID;
    }

    // Getters
    public String getCreatorUserUID() { return creatorUserUID; }
    public String getUserProfileImageUri() { return userProfileImageUri; }
    public String getCommentContent() { return commentContent; }
    public LocalTime getCommentCreationTime() { return commentCreationTime; }
    public CreationDate getCommentCreationDate() { return commentCreationDate; }
    public long getCommentCreationDateTimeAsLong() { return commentCreationDateTimeAsLong; }
    public String getCommentID() { return this.commentID; }
    public String getOwnerPostID() { return this.ownerPostID; }

    // Setters
    public void setCreatorUserUID(String creatorUserUID) { this.creatorUserUID = creatorUserUID; }
    public void setUserProfileImageUri(String userProfileImageUri) { this.userProfileImageUri = userProfileImageUri; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }
    public void setCommentCreationTime(LocalTime commentCreationTime) { this.commentCreationTime = commentCreationTime; }
    public void setCommentCreationDate(CreationDate commentCreationDate) { this.commentCreationDate = commentCreationDate; }
    public void setCommentCreationDateTimeAsLong(long commentCreationDateTimeAsLong) { this.commentCreationDateTimeAsLong = commentCreationDateTimeAsLong; }
    public void setCommentID(String commentID) { this.commentID = commentID; }
    public void setOwnerPostID(String ownerPostID) { this.ownerPostID = ownerPostID; }

    @Override
    public int compareTo(Comment other) {
        long comparison = other.getCommentCreationDateTimeAsLong() - this.getCommentCreationDateTimeAsLong();
        return (int)comparison;
    }

    @NonNull
    @Override
    public String toString() {
        return "Comment{" +
                "creatorUserUID='" + creatorUserUID + '\'' +
                ", userProfileImageUri='" + userProfileImageUri + '\'' +
                ", commentContent='" + commentContent + '\'' +
                ", commentCreationTime=" + commentCreationTime +
                ", commentCreationDate=" + commentCreationDate +
                ", commentCreationDateTimeAsLong=" + commentCreationDateTimeAsLong +
                '}';
    }
}