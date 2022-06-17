package com.example.buddies.common;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalTime;

public class Comment implements Comparable<Comment> {

    private String creatorUserUID;
    private String userProfileImageUri = null;
    private long belongsToPostCreationDateTimeAsLong;
    private String commentContent;
    private LocalTime commentCreationTime;
    private MyCreationDate commentCreationDate;
    private long commentCreationDateTimeAsLong;

    public Comment() { }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Comment(String i_CreatorUserUID, String i_UserProfileImageUri,
                   long i_BelongToPostCreationDateTimeAsLong, String i_CommentContent,
                   int i_CommentCreationYear, int i_CommentCreationMonth, int i_CommentCreationDay) {

        this.creatorUserUID = i_CreatorUserUID;
        this.userProfileImageUri = i_UserProfileImageUri;
        this.belongsToPostCreationDateTimeAsLong = i_BelongToPostCreationDateTimeAsLong;
        this.commentContent = i_CommentContent;
        // Gets current time based on user's current location.
        this.commentCreationTime = LocalTime.now();
        // Initialize date holder class.
        this.commentCreationDate = new MyCreationDate(i_CommentCreationYear, i_CommentCreationMonth,
                                                      i_CommentCreationDay);

        // Creating a long representing YYYY-MM-DD-HH-MM-SS for more convenient comparisons.
        // Meaning - newer posts will have higher value, and older posts will have lower value.
        String yearAsString = String.valueOf(i_CommentCreationYear);
        String monthAsString = i_CommentCreationMonth > 10 ?
                String.valueOf(i_CommentCreationMonth) : "0" + String.valueOf(i_CommentCreationMonth);
        String dayAsString = i_CommentCreationDay > 10 ?
                String.valueOf(i_CommentCreationDay) : "0" + String.valueOf(i_CommentCreationDay);
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
    }

    // Constructor for existing posts, loaded from FireBase.

    public Comment(String i_CreatorUserUID, String i_UserProfileImageUri,
                   long i_BelongToPostCreationDateTimeAsLong, String i_CommentContent,
                   LocalTime i_CommentCreationTime, long i_CommentCreationDateTimeAsLong,
                   int i_CommentCreationYear, int i_CommentCreationMonth, int i_CommentCreationDay) {

        this.creatorUserUID = i_CreatorUserUID;
        this.userProfileImageUri = i_UserProfileImageUri;
        this.belongsToPostCreationDateTimeAsLong = i_BelongToPostCreationDateTimeAsLong;
        this.commentContent = i_CommentContent;
        this.commentCreationTime = i_CommentCreationTime;
        // Initialize date holder class.
        this.commentCreationDate = new MyCreationDate(i_CommentCreationYear, i_CommentCreationMonth,
                                                      i_CommentCreationDay);
        this.commentCreationDateTimeAsLong = i_CommentCreationDateTimeAsLong;
    }

    // Getters
    public String getCreatorUserUID() { return creatorUserUID; }
    public String getUserProfileImageUri() { return userProfileImageUri; }
    public long getBelongsToPostCreationDateTimeAsLong() { return belongsToPostCreationDateTimeAsLong; }
    public String getCommentContent() { return commentContent; }
    public LocalTime getCommentCreationTime() { return commentCreationTime; }
    public MyCreationDate getCommentCreationDate() { return commentCreationDate; }
    public long getCommentCreationDateTimeAsLong() { return commentCreationDateTimeAsLong; }

    // Setters
    public void setCreatorUserUID(String creatorUserUID) { this.creatorUserUID = creatorUserUID; }
    public void setUserProfileImageUri(String userProfileImageUri) { this.userProfileImageUri = userProfileImageUri; }
    public void setBelongsToPostCreationDateTimeAsLong(long belongsToPostCreationDateTimeAsLong) { this.belongsToPostCreationDateTimeAsLong = belongsToPostCreationDateTimeAsLong; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }
    public void setCommentCreationTime(LocalTime commentCreationTime) { this.commentCreationTime = commentCreationTime; }
    public void setCommentCreationDate(MyCreationDate commentCreationDate) { this.commentCreationDate = commentCreationDate; }
    public void setCommentCreationDateTimeAsLong(long commentCreationDateTimeAsLong) { this.commentCreationDateTimeAsLong = commentCreationDateTimeAsLong; }

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
