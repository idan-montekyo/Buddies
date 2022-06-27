package com.example.buddies.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Comment;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

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

            Intent intent = new Intent(Intent.ACTION_SCREEN_OFF);
            intent.putExtra("message", remoteMessage.getData().get("message"));
            intent.putExtra(POST_AS_JSON_STRING_KEY, remoteMessage.getData().get(POST_AS_JSON_STRING_KEY));
            intent.putExtra(POST_CREATOR_USERNAME_KEY, remoteMessage.getData().get(POST_CREATOR_USERNAME_KEY));
            intent.putExtra(COMMENT_CREATOR_USERNAME_KEY, remoteMessage.getData().get(COMMENT_CREATOR_USERNAME_KEY));
            intent.putExtra(COMMENT_CONTENT_KEY, remoteMessage.getData().get(COMMENT_CONTENT_KEY));
            intent.putExtra(COMMENT_CREATOR_IMAGE_KEY, remoteMessage.getData().get(COMMENT_CREATOR_IMAGE_KEY));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    /**
     * This function is responsible for sending a POST request with the Data-Message info to the server.
     * @param context
     * @param post
     * @param comment
     * @param postCreatorProfile
     * @param commentCreatorProfile
     */
    public static void initializeDataMessageAfterCreatingACommentAndSendToServer(
                       Context context, Post post, Comment comment,
                       UserProfile postCreatorProfile, UserProfile commentCreatorProfile) {

        JSONObject rootObject = new JSONObject();
        try {

            String postAsJsonString = AppUtils.getGsonParser().toJson(post);

            rootObject.put("to", "/topics/" + post.getPostID());
            JSONObject object = new JSONObject();
            object.put("message", comment.getCommentContent());
            object.put(MyFirebaseMessagingService.POST_AS_JSON_STRING_KEY, postAsJsonString);
            object.put(MyFirebaseMessagingService.POST_CREATOR_USERNAME_KEY, postCreatorProfile.getFullName());
            object.put(MyFirebaseMessagingService.COMMENT_CREATOR_USERNAME_KEY, commentCreatorProfile.getFullName());
            object.put(MyFirebaseMessagingService.COMMENT_CONTENT_KEY, comment.getCommentContent());
            object.put(MyFirebaseMessagingService.COMMENT_CREATOR_IMAGE_KEY, commentCreatorProfile.getProfileImageUri());
            rootObject.put("data", object);

            String url = "https://fcm.googleapis.com/fcm/send";

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) { } // Irrelevant
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) { } // Irrelevant.
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + AppUtils.GetResourceStringValueByStringName("fcm_api_token_key", context));
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };

            queue.add(request);
            queue.start();
            // By now, the request has been sent to the server.

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
