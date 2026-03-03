package com.taiwanlife.teamwalk.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_BADGE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_MSG
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TITLE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TYPE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_URL
import com.taiwanlife.teamwalk.ui.main.MainActivity
import com.taiwanlife.teamwalk.utils.MyNotificationManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils.stringToNotificationType

class TWFirebaseMessagingService : FirebaseMessagingService() {
    /**
     *
     * @param remoteMessage
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(this, MainActivity::class.java)
        val msg = remoteMessage.getData()[NOTIFICATION_KEY_MSG]
        val url = remoteMessage.getData()[NOTIFICATION_KEY_URL]
        val type = remoteMessage.getData()[NOTIFICATION_KEY_TYPE]
        val badge = remoteMessage.getData()[NOTIFICATION_KEY_BADGE]
        val title = remoteMessage.getData()[NOTIFICATION_KEY_TITLE]

        val myNotificationManager = MyNotificationManager(this)

        if (!badge.isNullOrEmpty() && badge.toInt() > 0) {
            // 更新未讀數量
            val pendingIntent = MyNotificationManager.getPendingIntentForBadge(
                this,
                remoteMessage.sentTime.toInt(),
                badge.toInt(),
                title,
                msg,
                type,
                url
            )
            myNotificationManager.updateBadge(pendingIntent, badge.toInt())
            // 如果要使用後端帶給我們的Title和Msg
//            myNotificationManager.updateBadge(pendingIntent, badge.toInt(), title, msg)
        }

        val notificationType = stringToNotificationType(type)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = if (notificationType == Config.NotificationType.URL /* "L" */ && !url.isNullOrEmpty()) {
            // 收到需要外開瀏覽器的URL的網址 準備交由外面瀏覽器開啟
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
            // 先檢查是否有 Chrome 並將Package指定由Chrome開啟(Explicit Intent)
            val chromePackage = "com.android.chrome"
            browserIntent.setPackage(chromePackage)

            val finalIntent = if (isPackageInstalled(chromePackage)) {
                // 如果有 Chrome，直接使用
                browserIntent
            } else {
                // 如果沒有 Chrome，清除 package 設定並建立選擇器(Intent Chooser)
                browserIntent.setPackage(null)
                Intent.createChooser(browserIntent, "請選擇瀏覽器")
            }
            PendingIntent.getActivity(
                this,
                remoteMessage.getSentTime().toInt(),
                finalIntent,
                flags
            )
        } else {
            intent.putExtra(NOTIFICATION_KEY_URL, url)
            intent.putExtra(NOTIFICATION_KEY_TYPE, type)
            intent.putExtra(NOTIFICATION_KEY_TITLE, title)
            intent.putExtra(NOTIFICATION_KEY_MSG, msg)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            PendingIntent.getActivity(
                this,
                remoteMessage.getSentTime().toInt(),
                intent,
                flags
            )
        }
        val notificationId = remoteMessage.sentTime.toInt()
        myNotificationManager.sendFcmNotification(notificationId, title, msg, pendingIntent)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
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