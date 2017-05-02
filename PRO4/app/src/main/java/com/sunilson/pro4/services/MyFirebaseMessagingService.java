package com.sunilson.pro4.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.LivetickerActivity;
import com.sunilson.pro4.activities.MainActivity;
import com.sunilson.pro4.utilities.Constants;

/**
 * @author Linus Weiss
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Log.d(Constants.LOGGING_TAG, "Message data payload: " + remoteMessage.getData());
            String livetickerID = null;
            String author = null;
            String title = null;
            String type = null;

            type = remoteMessage.getData().get("type");

            if (type != null && type.equals("livetickerAdded")) {
                livetickerID = remoteMessage.getData().get("livetickerID");
                author = remoteMessage.getData().get("author");
                title = remoteMessage.getData().get("title");


                if (livetickerID != null && author != null && title != null) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_camera_black_24dp)
                                    .setAutoCancel(true)
                                    .setContentTitle(getString(R.string.new_liveticker_notification_title))
                                    .setContentText(author + " created a new Liveticker: " + title);

                    Intent resultIntent = new Intent(this, LivetickerActivity.class);
                    resultIntent.putExtra("livetickerID", livetickerID);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }
        }
    }
}


