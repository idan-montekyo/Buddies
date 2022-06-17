package com.example.buddies.interfaces.CommentCreationEvent;

public interface ICommentCreationResponseEventHandler {

    void onSuccessToCreateComment();
    void onFailureToCreateComment(Exception i_Reason);
}
