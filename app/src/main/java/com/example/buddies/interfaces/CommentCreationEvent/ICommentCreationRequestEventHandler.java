package com.example.buddies.interfaces.CommentCreationEvent;

import com.example.buddies.common.Comment;

public interface ICommentCreationRequestEventHandler {

    void onRequestToCreateComment(Comment i_Comment);
}
