package com.example.buddies.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.example.buddies.BuildConfig;
import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.fragments.HomeFragment;
import com.example.buddies.fragments.LoginFragment;
import com.example.buddies.fragments.ViewPostFragment;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileResponsesEventHandler;
import com.example.buddies.service.MyFirebaseMessagingService;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements IView,
                                                               IUpdateProfileResponsesEventHandler
{
    private final String OPEN_FRAGMENT_FROM_NOTIFICATION_TAG = "open_fragment_from_notification";

    boolean m_doubleBackToExitPressedOnce = false;
    Toast m_backToast;
    View m_RootMainActivity;
    Model m_Model = Model.getInstance();
    ViewModel m_ViewModel = ViewModel.getInstance();

    BroadcastReceiver receiver;

    NotificationManager notificationManager = null;
    NotificationCompat.Builder builder = null;
    RemoteViews remoteViews = null;
    NotificationTarget targetImageView;
    int NOTIF_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.m_ViewModel.registerForEvents((IView) this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_RootMainActivity = this.findViewById(R.id.root_main_activity);

        if (m_Model.isUserLoggedIn())
        {
            // Inflate Home-Fragment.
            HomeFragment homeFragment = new HomeFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_main_activity, homeFragment, HomeFragment.HOME_FRAGMENT_TAG)
                    .commit();

            // TODO: fix
//            isNotificationClicked(homeFragment);
        }
        else
        {
            // Inflate Login-Fragment.
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_main_activity, new LoginFragment(), LoginFragment.LOGIN_FRAGMENT_TAG)
                    .commit();
        }

        receiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onReceive(Context context, Intent intent) {

                String postAsJsonString = intent.getStringExtra(MyFirebaseMessagingService.POST_AS_JSON_STRING_KEY);
                handleNotificationSettings(postAsJsonString);

                NOTIF_ID += 1;

                targetImageView = new NotificationTarget(context, R.id.notification_image_image_view,
                        remoteViews, builder.build(), NOTIF_ID);

                Glide.with(context).asBitmap().override(100, 100).circleCrop().
                        load(Uri.parse(intent.getStringExtra(MyFirebaseMessagingService.COMMENT_CREATOR_IMAGE_KEY))).
                        into(targetImageView);

                remoteViews.setTextViewText(R.id.notification_comment_creators_name_text_view,
                        intent.getStringExtra(MyFirebaseMessagingService.COMMENT_CREATOR_USERNAME_KEY));
                remoteViews.setTextViewText(R.id.notification_comments_body_text_view,
                        intent.getStringExtra(MyFirebaseMessagingService.COMMENT_CONTENT_KEY));

                // Create and update notification
                notificationManager.notify(NOTIF_ID, builder.build());
            }
        };

        IntentFilter filter = new IntentFilter("message_received");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    // Requires double-back-press to exit while in Login/Home fragments.
    @Override
    public void onBackPressed()
    {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        // Not on Login / Home fragment.
        if(backStackCount > 0)
        {
            super.onBackPressed();
        }
        // On Login / Home fragment.
        else
        {
            // In case back button has already been pressed.
            if (m_doubleBackToExitPressedOnce)
            {
                m_backToast.cancel();
                super.onBackPressed();
                return;
            }

            this.m_doubleBackToExitPressedOnce = true;
            m_backToast = Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT);
            m_backToast.show();

            new Handler(Looper.getMainLooper())
                    .postDelayed(() -> m_doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    public void onSuccessToUpdateProfile()
    {
        Snackbar.make(this.m_RootMainActivity, "Profile has been successfully updated.", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onFailureToUpdateProfile(Exception i_Reason)
    {
        Snackbar.make(this.m_RootMainActivity, Objects.requireNonNull(i_Reason.getMessage()), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void handleNotificationSettings(String postAsJsonString) {

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        String channelID = "Buddies_notification_channel_id";
        if(Build.VERSION.SDK_INT >= 26) {
            CharSequence channelName = "Buddies Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder = new NotificationCompat.Builder(this, channelID);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // Open MainActivity -> HomeFragment when tapping on notification.
        Intent openMainActivityIntent = new Intent(this, MainActivity.class);
        openMainActivityIntent.putExtra(OPEN_FRAGMENT_FROM_NOTIFICATION_TAG, postAsJsonString);
        final int requestCode = 0;
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent openMainActivityPendingIntent = PendingIntent.
                getActivity(this, requestCode, openMainActivityIntent, PendingIntent.FLAG_MUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.notification_card_linear_layout, openMainActivityPendingIntent);

        builder.setContent(remoteViews);
//        builder.setAutoCancel(true);
//        builder.build().flags |= Notification.FLAG_AUTO_CANCEL;
    }

    private void isNotificationClicked(HomeFragment homeFragment) {

        Boolean bool = getIntent().hasExtra(OPEN_FRAGMENT_FROM_NOTIFICATION_TAG);
        if(bool) {

            String postAsJsonString = getIntent().getStringExtra(OPEN_FRAGMENT_FROM_NOTIFICATION_TAG);
            Post post = AppUtils.getGsonParser().fromJson(postAsJsonString, Post.class);

            UserProfile currentCreatorUserProfile = Model.getInstance().resolveUserProfileFromUID(post.getCreatorUserUID());
            String userProfileAsJsonString = AppUtils.getGsonParser().toJson(currentCreatorUserProfile);

            Bundle viewPostFragmentArguments = new Bundle();
            viewPostFragmentArguments.putString("userProfileJsonString", postAsJsonString);
            viewPostFragmentArguments.putString("currentPostJsonString", userProfileAsJsonString);

            ViewPostFragment postFragmentToLaunch = new ViewPostFragment();
            postFragmentToLaunch.setArguments(viewPostFragmentArguments);

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out) // popExit
                    .hide(homeFragment)
                    .add(R.id.root_main_activity, postFragmentToLaunch, ViewPostFragment.VIEW_POST_FRAGMENT_TAG)
                    .addToBackStack(null).commit();
        }
    }
}