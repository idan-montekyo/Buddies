package com.example.buddies.interfaces.PostCreationEvent;

import android.content.Context;

import com.example.buddies.common.Post;

public interface IPostCreationRequestEventHandler {

    void onRequestToCreatePost(Context i_Context, Post i_Post);
}
