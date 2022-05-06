package com.example.buddies.interfaces.LogoutEvent;

public interface ILogoutResponsesEventHandler
{
    void onSuccessToLogout();
    void onFailureToLogout(Exception i_Reason);

    /*
    void onSuccessToAnonymousLogout();
    void onFailureToAnonymousLogout(Exception i_Reason);
    */
}
