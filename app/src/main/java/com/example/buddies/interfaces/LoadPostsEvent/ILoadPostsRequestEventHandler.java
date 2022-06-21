package com.example.buddies.interfaces.LoadPostsEvent;

import com.example.buddies.enums.ePostType;

public interface ILoadPostsRequestEventHandler {

    void onRequestToLoadPosts(ePostType type);
    void onRequestToLoadPostsByCity(String i_SearchedCity);
}
