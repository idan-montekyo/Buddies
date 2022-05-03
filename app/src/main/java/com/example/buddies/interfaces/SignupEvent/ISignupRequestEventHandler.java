package com.example.buddies.interfaces.SignupEvent;

import android.content.Context;

import com.example.buddies.enums.eDogGender;

public interface ISignupRequestEventHandler
{
    void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age, eDogGender i_DogGender);
}
