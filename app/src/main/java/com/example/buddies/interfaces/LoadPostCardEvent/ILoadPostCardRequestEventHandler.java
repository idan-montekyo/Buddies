package com.example.buddies.interfaces.LoadPostCardEvent;

import com.example.buddies.adapters.PostAdapter;

public interface ILoadPostCardRequestEventHandler {

    void onRequestToLoadPostCard(String i_CreatorUserUID, PostAdapter i_PostAdapterToUpdate);
}
