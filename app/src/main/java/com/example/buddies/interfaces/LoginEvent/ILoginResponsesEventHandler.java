package com.example.buddies.interfaces.LoginEvent;

public interface ILoginResponsesEventHandler
{
    void onSuccessToLogin();
    void onFailureToLogin(Exception i_Reason);

    void onSuccessToAnonymousLogin();
    void onFailureToAnonymousLogin(Exception i_Reason);
}
