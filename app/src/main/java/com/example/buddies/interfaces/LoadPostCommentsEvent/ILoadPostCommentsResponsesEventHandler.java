package com.example.buddies.interfaces.LoadPostCommentsEvent;

import com.example.buddies.common.Comment;

import java.util.List;

public interface ILoadPostCommentsResponsesEventHandler
{
    void onSuccessToLoadPostComments(List<Comment> i_Comments);
    void onFailureToLoadPostComments(Exception i_Reason);
}
