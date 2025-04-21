/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/2
 */
public class TWFirebaseMessagingService extends FirebaseMessagingService {

    private static final String DEFAULT_CHANNEL = "default";

    /**
     *
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("url", remoteMessage.getData().get("url"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int)remoteMessage.getSentTime(), intent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int)remoteMessage.getSentTime(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.noti_channel_id))
                    .setSmallIcon(R.drawable.ic_firebase)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getBody())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[0]);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify((int)remoteMessage.getSentTime(), builder.build());
        }
    }
}
