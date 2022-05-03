package com.example.buddies.interfaces.SignupEvent;

import android.content.Context;

public interface ISignupRequestEventHandler
{
    void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age);
}
