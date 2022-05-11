package com.example.buddies.interfaces.LogoutEvent;

public interface ILogoutResponsesEventHandler {

    void onSuccessToLogout();
    void onFailureToLogout(Exception i_Reason);
}
