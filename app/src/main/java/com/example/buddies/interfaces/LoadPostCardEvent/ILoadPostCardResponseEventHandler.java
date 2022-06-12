package com.example.buddies.interfaces.LoadPostCardEvent;

import com.example.buddies.adapters.PostAdapter;
import com.example.buddies.common.UserProfile;

public interface ILoadPostCardResponseEventHandler {

    void onSuccessToLoadPostCard(UserProfile i_UserProfile, PostAdapter i_PostAdapterToUpdate);
    void onFailureToLoadPostCard(Exception i_Reason, PostAdapter i_PostAdapterToUpdate);
}
