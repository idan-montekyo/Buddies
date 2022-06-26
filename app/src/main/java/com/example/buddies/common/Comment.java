package com.example.buddies.common;

import android.icu.text.MessageFormat;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;

import java.time.LocalTime;

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

    private final String toStringFormat = "Comment{creatorUserUID='{0}', userProfileImageUri='{1}', commentContent='{2}', commentCreationDate={3}, commentCreationTime={4}, commentCreationDateTimeAsLong={5}}";

    private static final String creatorUserIDMainKey = "creatorUserUID";
    private static final String profileImageUriMainKey = "userProfileImageUri";
    private static final String commentContentMainKey = "commentContent";

    private static final String commentCreationTimeMainKey = "commentCreationTime";
    private static final String commentCreationTimeSubKeyOfHour = "hour";
    private static final String commentCreationTimeSubKeyOfMinute = "minute";
    private static final String commentCreationTimeSubKeyOfSecond = "second";
    private static final String commentCreationTimeSubKeyOfNano = "nano";

    private static final String commentCreationTimeAsLongMainKey = "commentCreationDateTimeAsLong";

    private static final String commentCreationDateMainKey = "commentCreationDate";
    private static final String commentCreationDateSubKeyOfYear = "creationYear";
    private static final String commentCreationDateSubKeyOfMonth = "creationMonth";
    private static final String commentCreationDateSubKeyOfDay = "creationDay";

    private static final String commentOwnerPostIDMainKey = "ownerPostID";
    private static final String commentIDMainKey = "commentID";



    public Comment() { }

    /**
     * Constructor of the class "Comment"
     * This constructor will be generally used where a new comment is created by the user
     * @param i_CreatorUserUID      - The UserID which belongs to the creator of the current post
     * @param i_UserProfileImageUri - The profile image uri of the commenter
     * @param i_CommentContent      - The content of the comment itself
     * @param i_OwnerPostID         - The ID of the post which this comment belongs to
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Comment(String i_CreatorUserUID, String i_UserProfileImageUri, String i_CommentContent, String i_OwnerPostID)
    {
        this.creatorUserUID = i_CreatorUserUID;
        this.userProfileImageUri = i_UserProfileImageUri;
        this.commentContent = i_CommentContent;

        // Gets current time based on user's current location.
        this.commentCreationTime = LocalTime.now();

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

    /**
     * Constructor of the class "Comment"
     * This constructor will be generally used where there's a need to load an existing comments from the FireBase.
     * @param i_CreatorUserUID                - The UserID which belongs to the creator of the current post
     * @param i_UserProfileImageUri           - The profile image uri of the commenter
     * @param i_CommentContent                - The content of the comment itself
     * @param i_CommentCreationTime           - The time when the comment created
     * @param i_CommentCreationDateTimeAsLong - A long which represents the creation time of the comment
     * @param i_CommentCreationYear           - The year when the comment has been created
     * @param i_CommentCreationMonth          - The month when the comment has been created
     * @param i_CommentCreationDay            - The day when the comment has been created
     * @param i_OwnerPostID                   - The ID of the post which this comment belongs to
     * @param i_CommentID                     - The ID of the comment itself
     */
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
    public int compareTo(Comment other)
    {
        long comparison = other.getCommentCreationDateTimeAsLong() - this.getCommentCreationDateTimeAsLong();
        return (int)comparison;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public String toString()
    {
        Object[] args = { creatorUserUID, userProfileImageUri, commentContent, commentCreationDate, commentCreationTime,commentCreationDateTimeAsLong };
        MessageFormat fmt = new MessageFormat(this.toStringFormat);

        /*
        String.format("Comment{creatorUserUID='%s', userProfileImageUri='%s', commentContent='%', commentCreationDate=%s, commentCreationTime=%s, commentCreationDateTimeAsLong=%s}",
                null,
                null,
                null,
                null,
                null,
                null);
        */

        /*
        return "Comment{" +
                "creatorUserUID='" + creatorUserUID + '\'' +
                ", userProfileImageUri='" + userProfileImageUri + '\'' +
                ", commentContent='" + commentContent + '\'' +
                ", commentCreationTime=" + commentCreationTime +
                ", commentCreationDate=" + commentCreationDate +
                ", commentCreationDateTimeAsLong=" + commentCreationDateTimeAsLong +
                '}';
        */

        return fmt.format(args);
    }

    public static Comment parse(DataSnapshot commentOfPost) throws Exception
    {
        String creatorUserUID = (String) commentOfPost.child(creatorUserIDMainKey).getValue();
        String userProfileImageUri = (String) commentOfPost.child(profileImageUriMainKey).getValue();
        String commentContent = (String) commentOfPost.child(commentContentMainKey).getValue();

        long hour   = (long) commentOfPost.child(commentCreationTimeMainKey).child(commentCreationTimeSubKeyOfHour).getValue();
        long minute = (long) commentOfPost.child(commentCreationTimeMainKey).child(commentCreationTimeSubKeyOfMinute).getValue();
        long nano   = (long) commentOfPost.child(commentCreationTimeMainKey).child(commentCreationTimeSubKeyOfNano).getValue();
        long second = (long) commentOfPost.child(commentCreationTimeMainKey).child(commentCreationTimeSubKeyOfSecond).getValue();

        LocalTime commentCreationTime = LocalTime.of((int)hour, (int)minute, (int)second, (int)nano);

        long commentCreationDateTimeAsLong = (long) commentOfPost.child(commentCreationTimeAsLongMainKey).getValue();
        long commentCreationYear = (long) commentOfPost.child(commentCreationDateMainKey).child(commentCreationDateSubKeyOfYear).getValue();
        long commentCreationMonth = (long) commentOfPost.child(commentCreationDateMainKey).child(commentCreationDateSubKeyOfMonth).getValue();
        long commentCreationDay = (long) commentOfPost.child(commentCreationDateMainKey).child(commentCreationDateSubKeyOfDay).getValue();
        String ownerPostID = (String) commentOfPost.child(commentOwnerPostIDMainKey).getValue();
        String commentID = (String) commentOfPost.child(commentIDMainKey).getValue();

        Comment newComment = new Comment(creatorUserUID, userProfileImageUri, commentContent, commentCreationTime, commentCreationDateTimeAsLong, (int)commentCreationYear, (int)commentCreationMonth, (int)commentCreationDay, ownerPostID, commentID);

        return newComment;
    }
}