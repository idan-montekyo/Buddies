package com.example.buddies.interfaces.UploadImageEvent;

import android.net.Uri;

public interface IUploadImageRequestEventHandler
{
    void onRequestToUploadImage(Uri i_PathOfFileInFilesystem);
}
