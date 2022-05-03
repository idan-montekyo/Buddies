package com.example.buddies.interfaces.LoginEvent;

public interface ILoginRequestEventHandler
{
    void onRequestToLogin(String i_UserName, String i_Password);
    void onRequestToAnonymousLogin();
}
