package com.example.buddies.interfaces.ResolveUIDToUserProfileEvent;

import com.example.buddies.common.UserProfile;
import com.example.buddies.interfaces.MVVM.IView;

public interface IResolveUIDToUserProfileResponsesEventHandler
{
    void onSuccessToResolveUIDToUserProfile(UserProfile i_ResolvedUserProfile, IView i_Caller);
    void onFailureToResolveUIDToUserProfile(Exception i_Reason, IView i_Caller);
}
