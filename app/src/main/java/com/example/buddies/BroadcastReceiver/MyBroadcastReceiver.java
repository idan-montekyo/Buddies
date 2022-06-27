package com.example.buddies.BroadcastReceiver;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.example.buddies.BuildConfig;
import com.example.buddies.R;
import com.example.buddies.activities.MainActivity;
import com.example.buddies.fragments.SettingsFragment;
import com.example.buddies.service.MyFirebaseMessagingService;

public class MyBroadcastReceiver extends Service {

    private static BroadcastReceiver m_Receiver;

    SharedPreferences m_SP;

    private NotificationManager m_NotificationManager = null;
    private NotificationCompat.Builder m_Builder = null;
    private RemoteViews m_RemoteViews = null;
    private NotificationTarget m_TargetImageView;
    private int NOTIF_ID = 0;

    private final String OPEN_FRAGMENT_FROM_NOTIFICATION_TAG = "open_fragment_from_notification";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() { initializeReceiver(); }

    @Override
    public void onDestroy() {
        if (m_Receiver != null) {
            unregisterReceiver(m_Receiver);
            m_Receiver = null;
        }
    }

    /**
     * This function is responsible for initializing the receiver and it's onReceive method.
     * It also initializes RemoteViews widget in the notification, and creates a new notification.
     */
    private void initializeReceiver() {

        m_SP = getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        m_Receiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onReceive(Context context, Intent intent) {

                if (m_SP.getBoolean(SettingsFragment.SP_NOTIFICATIONS_KEY, true)) {

                    String postAsJsonString = intent.getStringExtra(MyFirebaseMessagingService.POST_AS_JSON_STRING_KEY);
                    handleNotificationSettings(postAsJsonString);

                    NOTIF_ID += 1;

                    m_TargetImageView = new NotificationTarget(context, R.id.notification_image_image_view,
                            m_RemoteViews, m_Builder.build(), NOTIF_ID);

                    Glide.with(context).asBitmap().override(100, 100).circleCrop().
                            load(Uri.parse(intent.getStringExtra(MyFirebaseMessagingService.COMMENT_CREATOR_IMAGE_KEY))).
                            into(m_TargetImageView);

                    m_RemoteViews.setTextViewText(R.id.notification_comment_creators_name_text_view,
                            intent.getStringExtra(MyFirebaseMessagingService.COMMENT_CREATOR_USERNAME_KEY));
                    m_RemoteViews.setTextViewText(R.id.notification_comments_body_text_view,
                            intent.getStringExtra(MyFirebaseMessagingService.COMMENT_CONTENT_KEY));

                    // Create and update notification
                    m_NotificationManager.notify(NOTIF_ID, m_Builder.build());
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(m_Receiver, filter);
    }

    /**
     * This function is responsible for creating a NotificationCompat.Builder, initialize it,
     * set it's content - a RemoteViews widget, and also navigating to MainActivity when clicked.
     * @param postAsJsonString
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void handleNotificationSettings(String postAsJsonString) {

        m_NotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        String channelID = "Buddies_notification_channel_id";
        if(Build.VERSION.SDK_INT >= 26) {
            CharSequence channelName = "Buddies Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);

            m_NotificationManager.createNotificationChannel(notificationChannel);
        }

        m_Builder = new NotificationCompat.Builder(this, channelID);
        m_Builder.setSmallIcon(R.mipmap.ic_launcher);

        m_RemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // Open MainActivity -> HomeFragment when tapping on notification.
        Intent openMainActivityIntent = new Intent(this, MainActivity.class);
        openMainActivityIntent.putExtra(OPEN_FRAGMENT_FROM_NOTIFICATION_TAG, postAsJsonString);
        final int requestCode = 0;
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent openMainActivityPendingIntent = PendingIntent.
                getActivity(this, requestCode, openMainActivityIntent, PendingIntent.FLAG_MUTABLE);
        m_RemoteViews.setOnClickPendingIntent(R.id.notification_card_linear_layout, openMainActivityPendingIntent);

        m_Builder.setContent(m_RemoteViews);
    }
}
