package com.example.buddies.interfaces.CommentCreationEvent;

import com.example.buddies.common.Comment;

public interface ICommentCreationResponseEventHandler {

    void onSuccessToCreateComment(Comment i_Comment);
    void onFailureToCreateComment(Exception i_Reason);
}
