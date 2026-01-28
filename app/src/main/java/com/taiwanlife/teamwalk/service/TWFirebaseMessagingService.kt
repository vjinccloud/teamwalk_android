package com.taiwanlife.teamwalk.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_MSG
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TITLE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TYPE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_URL
import com.taiwanlife.teamwalk.ui.main.MainActivity
import com.taiwanlife.teamwalk.utils.MyNotificationManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager

class TWFirebaseMessagingService : FirebaseMessagingService() {
    /**
     *
     * @param remoteMessage
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.getNotification() != null) {
            val notification = remoteMessage.getNotification()
            val intent = Intent(this, MainActivity::class.java)
            val title = remoteMessage.getData()[NOTIFICATION_KEY_TITLE]
            val msg = remoteMessage.getData()[NOTIFICATION_KEY_MSG]


            intent.putExtra(NOTIFICATION_KEY_URL, remoteMessage.getData()[NOTIFICATION_KEY_URL])
            intent.putExtra(NOTIFICATION_KEY_TYPE, remoteMessage.getData()[NOTIFICATION_KEY_TYPE])
            intent.putExtra(NOTIFICATION_KEY_TITLE, title)
            intent.putExtra(NOTIFICATION_KEY_MSG, msg)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this,
                    remoteMessage.getSentTime().toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    this,
                    remoteMessage.getSentTime().toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val notificationId = remoteMessage.sentTime.toInt()
            val myNotificationManager = MyNotificationManager(this)
            myNotificationManager.sendFcmNotification(notificationId, title, msg, pendingIntent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        SecuredPreferenceStoreManager.simpleEditAndApply(
            Config.SP_FCM_IDENTIFIER,
            token
        )
    }
}