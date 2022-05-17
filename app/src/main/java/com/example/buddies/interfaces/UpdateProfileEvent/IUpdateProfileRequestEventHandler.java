package com.example.buddies.interfaces.UpdateProfileEvent;

import android.content.Context;

import com.example.buddies.enums.eDogGender;

public interface IUpdateProfileRequestEventHandler
{
    void onRequestToUpdateProfile(Context i_Context, String i_FullName, String i_Age, eDogGender i_DogGender, String i_ProfileImage);
}
