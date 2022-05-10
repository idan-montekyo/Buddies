package com.example.buddies.interfaces.LoginEvent;

import android.content.Context;

public interface ILoginRequestEventHandler
{
    void onRequestToLogin(Context i_Context, String i_UserName, String i_Password);
    void onRequestToAnonymousLogin(Context i_Context);
}
