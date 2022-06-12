package com.example.buddies.interfaces.LoadPostsEvent;

import com.example.buddies.common.Post;

import java.util.List;

public interface ILoadPostsResponseEventHandler {

    void onSuccessToLoadPosts(List<Post> i_PostsList);
    void onFailureToLoadPosts(Exception i_Reason);
}
