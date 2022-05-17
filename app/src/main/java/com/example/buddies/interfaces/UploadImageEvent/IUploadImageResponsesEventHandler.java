package com.example.buddies.interfaces.UploadImageEvent;

import android.net.Uri;

public interface IUploadImageResponsesEventHandler
{
    void onSuccessToUploadImage(Uri i_PathOfFileInCloud);
    void onFailureToUploadImage(Exception i_Reason);
}
