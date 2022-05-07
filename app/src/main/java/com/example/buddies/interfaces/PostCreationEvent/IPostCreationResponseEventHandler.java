package com.example.buddies.interfaces.PostCreationEvent;

public interface IPostCreationResponseEventHandler {

    void onSuccessToCreatePost();
    void onFailureToCreatePost(Exception i_Reason);
}
