package com.example.buddies.interfaces.PostCreationEvent;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public interface IPostCreationRequestEventHandler {

    void onRequestToCreatePost(Context i_Context, String i_UserID, String i_CityOfMeeting, String i_StreetOfMeeting, String i_DateOfMeeting, String i_TimeOfMeeting, LatLng i_LocationOfMeeting, String i_ContentOfPost);
}
