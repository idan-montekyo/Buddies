package com.example.buddies.interfaces.LoadPostCardEvent;

import com.example.buddies.adapters.PostAdapter;

public interface ILoadPostCardRequestEventHandler {

    void onLoadPostCard(String i_CreatorUserUID, PostAdapter i_PostAdapterToUpdate);
}
