package com.example.buddies.interfaces.LoadPostsEvent;

import com.example.buddies.enums.ePostType;

public interface ILoadPostsRequestEventHandler {

    void onLoadPosts(ePostType type);
    void onLoadPostsByCity(String i_SearchedCity);
}
