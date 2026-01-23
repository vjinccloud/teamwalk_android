package com.taiwanlife.teamwalk.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.main.MainActivity
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import timber.log.Timber

class TWFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val KEY_URL = "url"
    }

    /**
     *
     * @param remoteMessage
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.getNotification() != null) {
            val notification = remoteMessage.getNotification()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(KEY_URL, remoteMessage.getData()["url"])
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

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


            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, Config.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_firebase)
                    .setContentTitle(notification!!.title)
                    .setContentText(notification.body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(LongArray(0))

            val notificationManager = NotificationManagerCompat.from(this)

            try {
                // 到這裡如果使用者沒有給權限可能會出現錯誤 把他接起來
                notificationManager.notify(remoteMessage.getSentTime().toInt(), builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
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