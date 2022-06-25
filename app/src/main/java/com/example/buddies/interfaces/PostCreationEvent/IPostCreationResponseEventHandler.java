package com.example.buddies.interfaces.PostCreationEvent;

import com.example.buddies.common.Post;

public interface IPostCreationResponseEventHandler {

    void onSuccessToCreatePost(Post i_Post);
    void onFailureToCreatePost(Exception i_Reason);
}
