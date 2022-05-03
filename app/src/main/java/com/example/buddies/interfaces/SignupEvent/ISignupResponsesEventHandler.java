package com.example.buddies.interfaces.SignupEvent;

public interface ISignupResponsesEventHandler
{
    void onSuccessToSignup();
    void onFailureToSignup(Exception i_Reason);
}
