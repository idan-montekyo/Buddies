package com.example.buddies.interfaces.UpdateProfileEvent;

public interface IUpdateProfileResponsesEventHandler
{
    void onSuccessToUpdateProfile();
    void onFailureToUpdateProfile(Exception i_Reason);
}
