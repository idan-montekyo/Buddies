package com.example.buddies.interfaces.CommentCreationEvent;

public interface ICommentCreationRequestEventHandler {

    void onRequestToCreateComment(String i_CreatorUserUID, String i_UserProfileImageUri, String i_CommentContent, String i_PostID);
}
