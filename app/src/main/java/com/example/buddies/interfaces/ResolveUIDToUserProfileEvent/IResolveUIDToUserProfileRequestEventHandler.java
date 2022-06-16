package com.example.buddies.interfaces.ResolveUIDToUserProfileEvent;

import com.example.buddies.interfaces.MVVM.IView;

public interface IResolveUIDToUserProfileRequestEventHandler
{
    void onRequestToResolveUIDToUserProfile(String i_UserIDToResolve, IView i_Caller);
}
