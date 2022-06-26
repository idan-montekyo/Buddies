package com.example.buddies.common;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.time.LocalTime;

public class Post implements Comparable<Post>
{
    private CreationDate m_PostCreationDate           = null;
    private LatLng       m_MeetingLocation            = null;
    private LocalTime    m_PostCreationTime           = null;
    private long         m_PostCreationDateTimeAsLong = 0;
    private String       m_CreatorUserUID             = null;
    private String       m_MeetingCity                = null;
    private String       m_MeetingStreet              = null;
    private CreationDate m_MeetingDate                = null;
    private String       m_MeetingTime                = null;
    private String       m_PostContent                = null;
    private String       m_PostID                     = null;

    public Post() { }

    /**
     * Constructor of the class "Post"
     * This constructor will be generally used where a new post is created by the user
     * @param i_CreatorUserUID  - The UserID which belongs to the creator of the current post
     * @param i_MeetingCity     - The city where the meeting will take place
     * @param i_MeetingStreet   - The street where the meeting will take place
     * @param i_MeetingDate     - The date when the meeting will take place
     * @param i_MeetingTime     - The time when the meeting will take place
     * @param i_MeetingLocation - The location of the meeting, represented by coordinates
     * @param i_PostContent     - The content of the post itself
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Post(String i_CreatorUserUID, String i_MeetingCity, String i_MeetingStreet,
                CreationDate i_MeetingDate, String i_MeetingTime, LatLng i_MeetingLocation, String i_PostContent)
    {
        this.m_CreatorUserUID = i_CreatorUserUID;
        this.m_MeetingCity = i_MeetingCity;
        this.m_MeetingStreet = i_MeetingStreet;
        this.m_MeetingDate = i_MeetingDate;
        this.m_MeetingTime = i_MeetingTime;
        this.m_MeetingLocation = i_MeetingLocation;
        this.m_PostContent = i_PostContent;

        // Gets current time based on user's current location.
        this.m_PostCreationTime = LocalTime.now();

        // Initialize date holder class.
        this.m_PostCreationDate = CreationDate.now();

        // Creating a long representing YYYY-MM-DD-HH-MM-SS for more convenient comparisons.
        // Meaning - newer posts will have higher value, and older posts will have lower value.
        String yearAsString = String.valueOf(this.m_PostCreationDate.getCreationYear());
        String monthAsString = this.m_PostCreationDate.getCreationMonth() > 10 ?
                String.valueOf(this.m_PostCreationDate.getCreationMonth()) : "0" + String.valueOf(this.m_PostCreationDate.getCreationMonth());
        String dayAsString = this.m_PostCreationDate.getCreationDay() > 10 ?
                String.valueOf(this.m_PostCreationDate.getCreationDay()) : "0" + String.valueOf(this.m_PostCreationDate.getCreationDay());
        String hourAsString = this.m_PostCreationTime.getHour() > 10 ?
                String.valueOf(this.m_PostCreationTime.getHour()) :
                "0" + String.valueOf(this.m_PostCreationTime.getHour());
        String minuteAsString = this.m_PostCreationTime.getMinute() > 10 ?
                String.valueOf(this.m_PostCreationTime.getMinute()) :
                "0" + String.valueOf(this.m_PostCreationTime.getMinute());
        String secondAsString = this.m_PostCreationTime.getSecond() > 10 ?
                String.valueOf(this.m_PostCreationTime.getSecond()) :
                "0" + String.valueOf(this.m_PostCreationTime.getSecond());
        this.m_PostCreationDateTimeAsLong = Long.parseLong(yearAsString + monthAsString + dayAsString
                                                         + hourAsString + minuteAsString + secondAsString);
    }

    /**
     * Constructor of the class "Post"
     * This constructor will be generally used where there's a need to load an existing posts from the FireBase.
     * @param i_CreatorUserUID             - The UserID which belongs to the creator of the current post
     * @param i_MeetingCity                - The city where the meeting will take place
     * @param i_MeetingStreet              - The street where the meeting will take place
     * @param i_MeetingDate                - The date when the meeting will take place
     * @param i_MeetingTime                - The time when the meeting will take place
     * @param i_MeetingLocation            - The location of the meeting, represented by coordinates
     * @param i_PostContent                - The content of the post itself
     * @param i_PostCreationTime           - The time when the post created
     * @param i_PostCreationDateTimeAsLong - A long which represents the creation time of the post
     * @param i_PostCreationYear           - The year when the post has been created
     * @param i_PostCreationMonth          - The month when the post has been created
     * @param i_PostCreationDay            - The day when the post has been created
     * @param i_PostID                     - The ID of the post itself
     */
    public Post(String i_CreatorUserUID, String i_MeetingCity, String i_MeetingStreet,
                CreationDate i_MeetingDate, String i_MeetingTime, LatLng i_MeetingLocation, String i_PostContent,
                LocalTime i_PostCreationTime, long i_PostCreationDateTimeAsLong,
                int i_PostCreationYear, int i_PostCreationMonth, int i_PostCreationDay, String i_PostID)
    {
        this.m_CreatorUserUID = i_CreatorUserUID;
        this.m_MeetingCity = i_MeetingCity;
        this.m_MeetingStreet = i_MeetingStreet;
        this.m_MeetingDate = i_MeetingDate;
        this.m_MeetingTime = i_MeetingTime;
        this.m_MeetingLocation = i_MeetingLocation;
        this.m_PostContent = i_PostContent;
        this.m_PostCreationTime = i_PostCreationTime;
        // Initialize date holder class.
        this.m_PostCreationDate = new CreationDate(i_PostCreationYear, i_PostCreationMonth,
                                                   i_PostCreationDay);
        this.m_PostCreationDateTimeAsLong = i_PostCreationDateTimeAsLong;
        this.m_PostID = i_PostID;
    }

    // Getters
    public String getCreatorUserUID() { return m_CreatorUserUID; }
    public String getMeetingCity() { return m_MeetingCity; }
    public String getMeetingStreet() { return m_MeetingStreet; }
    public CreationDate getMeetingDate() { return m_MeetingDate; }
    public String getMeetingTime() { return m_MeetingTime; }
    public LatLng getMeetingLocation() { return m_MeetingLocation; }
    public String getPostContent() { return m_PostContent; }
    public LocalTime getPostCreationTime() { return m_PostCreationTime; }
    public CreationDate getPostCreationDate() { return m_PostCreationDate; }
    public long getPostCreationDateTimeAsLong() { return m_PostCreationDateTimeAsLong; }
    public String getPostID() { return this.m_PostID; }

    // Setters
    public void setMeetingCity(String m_MeetingCity) { this.m_MeetingCity = m_MeetingCity; }
    public void setMeetingStreet(String m_MeetingStreet) { this.m_MeetingStreet = m_MeetingStreet; }
    public void setMeetingDate(CreationDate m_MeetingDate) { this.m_MeetingDate = m_MeetingDate; }
    public void setMeetingTime(String m_MeetingTime) { this.m_MeetingTime = m_MeetingTime; }
    public void setMeetingLocation(LatLng m_MeetingLocation) { this.m_MeetingLocation = m_MeetingLocation; }
    public void setPostContent(String m_PostContent) { this.m_PostContent = m_PostContent; }
    public void setPostID(String m_PostID) { this.m_PostID = m_PostID; }

    @Override
    public int compareTo(Post other) {
        long comparison = other.getPostCreationDateTimeAsLong() - this.getPostCreationDateTimeAsLong();
        return (int)comparison;
    }

    @NonNull
    @Override
    public String toString() {
        return "Post{" +
                "creatorUserUID='" + m_CreatorUserUID + '\'' +
                ", meetingCity='" + m_MeetingCity + '\'' +
                ", meetingStreet='" + m_MeetingStreet + '\'' +
                ", meetingDate='" + m_MeetingDate + '\'' +
                ", meetingTime='" + m_MeetingTime + '\'' +
                ", meetingLocation=" + m_MeetingLocation +
                ", postContent='" + m_PostContent + '\'' +
                ", postCreationTime=" + m_PostCreationTime +
                ", postCreationDate=" + m_PostCreationDate +
                ", postCreationDateTimeAsLong=" + m_PostCreationDateTimeAsLong +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Post parse(DataSnapshot post) throws Exception
    {
        String userID = (String) post.child("creatorUserUID").getValue();
        String cityOfMeeting = (String) post.child("meetingCity").getValue();
        String streetOfMeeting = (String) post.child("meetingStreet").getValue();

        // Get the meeting date
        Long meetingYear = (long) post.child("meetingDate").child("creationYear").getValue();
        Long meetingMonth = (long) post.child("meetingDate").child("creationMonth").getValue();
        Long meetingDay = (long) post.child("meetingDate").child("creationDay").getValue();
        CreationDate dateOfMeeting = new CreationDate(meetingYear.intValue(), meetingMonth.intValue(), meetingDay.intValue());

        String timeOfMeeting = (String) post.child("meetingTime").getValue();

        LatLng locationOfMeeting = new LatLng(
                (double) post.child("meetingLocation").child("latitude").getValue(),
                (double) post.child("meetingLocation").child("longitude").getValue()
        );

        String contentOfPost = (String) post.child("postContent").getValue();

        // Get the time of post creation
        Long localHour = (long)post.child("postCreationTime").child("hour").getValue();
        Long localMinute = (long)post.child("postCreationTime").child("minute").getValue();
        Long localSecond = (long)post.child("postCreationTime").child("second").getValue();
        Long localNano = (long)post.child("postCreationTime").child("nano").getValue();

        LocalTime postCreationTime = LocalTime.of(localHour.intValue(), localMinute.intValue(),
                localSecond.intValue(), localNano.intValue());

        Long postCreationDateTimeAsLong = (long) post.child("postCreationDateTimeAsLong").getValue();

        // Get the date of post creation
        Long creationYear = (long) post.child("postCreationDate").child("creationYear").getValue();
        Long creationMonth = (long) post.child("postCreationDate").child("creationMonth").getValue();
        Long creationDay = (long) post.child("postCreationDate").child("creationDay").getValue();

        String postID = (String) post.child("postID").getValue();

        Post newPost = new Post(userID,
                cityOfMeeting,
                streetOfMeeting,
                dateOfMeeting,
                timeOfMeeting,
                locationOfMeeting,
                contentOfPost,
                postCreationTime, postCreationDateTimeAsLong,
                creationYear.intValue(), creationMonth.intValue(), creationDay.intValue(), postID);

        return newPost;
    }
}
