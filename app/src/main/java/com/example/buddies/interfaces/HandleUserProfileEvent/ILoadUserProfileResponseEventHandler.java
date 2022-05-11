package com.example.buddies.interfaces.HandleUserProfileEvent;

import com.example.buddies.common.UserProfile;

public interface ILoadUserProfileResponseEventHandler {

    void onSuccessToLoadProfile(UserProfile i_UserProfile);
    void onFailureToLoadProfile(Exception i_Reason);
}
