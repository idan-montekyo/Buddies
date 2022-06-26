package com.example.buddies.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String API_TOKEN_KEY = "AAAAeAqZc38:APA91bHxOAprs_LKN_UBbrVpuy53xTsUoBZxvdYi386PPsKh8-caPc2dmsffqqAfhL7QdtQWT2WMFnemdPG7MiIyJT-z7qJvUhY4wyb3DcR26Iz1PmnLezsEqnN9R1fdoIhmzNOugWUT";

    public static final String POST_AS_JSON_STRING_KEY = "post_as_json_string";
    public static final String POST_CREATOR_USERNAME_KEY = "post_creator_username";
    public static final String COMMENT_CREATOR_USERNAME_KEY = "comment_creator_username";
    public static final String COMMENT_CONTENT_KEY = "comment_content";
    public static final String COMMENT_CREATOR_IMAGE_KEY = "comment_creator_image";

    @Override
    public void onNewToken(@NonNull String token) { super.onNewToken(token); }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // If the remote message received is a Data-Message:
        if (remoteMessage.getData().size() > 0) {

            System.out.println("data-message: " + remoteMessage.getData());

            Intent intent = new Intent("message_received");
            intent.putExtra("message", remoteMessage.getData().get("message"));
            intent.putExtra(POST_AS_JSON_STRING_KEY, remoteMessage.getData().get(POST_AS_JSON_STRING_KEY));
            intent.putExtra(POST_CREATOR_USERNAME_KEY, remoteMessage.getData().get(POST_CREATOR_USERNAME_KEY));
            intent.putExtra(COMMENT_CREATOR_USERNAME_KEY, remoteMessage.getData().get(COMMENT_CREATOR_USERNAME_KEY));
            intent.putExtra(COMMENT_CONTENT_KEY, remoteMessage.getData().get(COMMENT_CONTENT_KEY));
            intent.putExtra(COMMENT_CREATOR_IMAGE_KEY, remoteMessage.getData().get(COMMENT_CREATOR_IMAGE_KEY));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }
}
