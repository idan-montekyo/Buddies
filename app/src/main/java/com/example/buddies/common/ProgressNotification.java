package com.example.buddies.common;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class ProgressNotification
{
    Notification notification;

    Context m_CurrentContext = null;
    NotificationManager notificationManager = null;
    NotificationCompat.Builder builder;
    final String m_NotificationPrimaryCategory = "Progress Notification";
    eSecondaryNotificationTypes m_CurrentSubCategoryOfNotification = null;
    String m_CurrentTitle = null;
    String m_CurrentText = null;
    double m_CurrentPercentage = 0.0;

    public enum eSecondaryNotificationTypes {

        NOTIFICATIONS_OF_UPLOADS_PROGRESSES___UPLOAD_IMAGE_PROGRESS(1),
        NOTIFICATIONS_OF_UPLOADS_PROGRESSES___UPLOAD_POST_PROGRESS(2),
        NOTIFICATIONS_OF_MEETINGS_UPDATES___INCOMING_MESSAGE_FOR_MEETING(3),
        NOTIFICATIONS_OF_MEETINGS_UPDATES___NEW_PARTICIPANT_IN_THE_MEETING(4),
        NOTIFICATIONS_OF_MEETINGS_UPDATES___PARTICIPANT_LEFT_THE_MEETING(5);

        private final int value;

        eSecondaryNotificationTypes(final int newValue)
        {
            value = newValue;
        }

        public int getValue() { return value; }
    }

    public ProgressNotification(Context i_Context, eSecondaryNotificationTypes i_SubCategoryOfNotification, String i_Title, String i_Text) {
        super();

        this.m_CurrentContext = i_Context;
        this.m_CurrentSubCategoryOfNotification = i_SubCategoryOfNotification;
        this.m_CurrentTitle = i_Title;
        this.m_CurrentText = i_Text;

        this.createNotification();
    }

    private void createNotification() {
        this.notificationManager = (NotificationManager)this.m_CurrentContext.getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(this.m_CurrentSubCategoryOfNotification.toString(), this.m_NotificationPrimaryCategory, NotificationManager.IMPORTANCE_DEFAULT);
            this.notificationManager.createNotificationChannel(channel);
        }

        builder  = new NotificationCompat.Builder(this.m_CurrentContext, this.m_CurrentSubCategoryOfNotification.toString());

        builder.setContentTitle(this.m_CurrentTitle)
                .setContentText(this.m_CurrentText)
                .setProgress(100, (int)this.m_CurrentPercentage,false)
                .setSmallIcon(android.R.drawable.ic_dialog_info);
    }

    public void launchNotification() {
        this.notification = builder.build();
        this.notificationManager.notify(this.m_CurrentSubCategoryOfNotification.getValue(), this.notification);
    }

    public void updateNotification(String i_NewTitle, String i_NewText, double i_NewPercentage) {
        if (i_NewPercentage < 100.0) {
            this.builder.setProgress(100, (int) i_NewPercentage, false);
            this.builder.setContentTitle(i_NewTitle);
            this.builder.setContentText(i_NewText);
        } else {
            this.builder.setProgress(100, 0, false);
            this.builder.setContentTitle("Upload Finished !");
            this.builder.setContentText("Upload Finished !");
        }

        this.launchNotification();
    }

    @NonNull
    @Override
    public String toString() { return super.toString(); }
}
