package com.taiwanlife.teamwalk.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.BADGE_NOTIFICATION_ID
import com.taiwanlife.teamwalk.Config.CHANNEL_ID
import com.taiwanlife.teamwalk.Config.CHANNEL_ID_FOR_BADGE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_MSG
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TITLE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TYPE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_URL
import com.taiwanlife.teamwalk.MyApplication
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.main.MainActivity
import java.util.Locale

class MyNotificationManager(private val context: Context) {
    companion object {
        fun getPendingIntentForBadge(
            context: Context,
            requestCode: Int,
            unreadCount: Int,
            title: String? = context.getString(R.string.notification_badge_title),
            msg: String? = String.format(
                Locale.getDefault(),
                context.getString(R.string.notification_badge_message),
                unreadCount
            ),
            type: String? = Config.NotificationType.NONE.v,
            url: String? = "",
            notificationId: Int = BADGE_NOTIFICATION_ID
        ): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(NOTIFICATION_KEY_URL, url)
            intent.putExtra(NOTIFICATION_KEY_TYPE, type)
            intent.putExtra(NOTIFICATION_KEY_TITLE, title)
            intent.putExtra(NOTIFICATION_KEY_MSG, msg)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        MyApplication.createNotificationChannel()
    }

//    fun addNotification(message: String, notificationId: Int = BADGE_NOTIFICATION_ID) {
//        val notification = buildNotification(-1, message)
//        notificationManager.notify(notificationId, notification)
//    }


    /**
     * 更新或建立一個 Badge 通知
     */
    fun updateBadge(
        pendingIntent: PendingIntent,
        unreadCount: Int,
        title: String? = context.getString(R.string.notification_badge_title),
        msg: String? = String.format(
            Locale.getDefault(), context.getString(R.string.notification_badge_message), unreadCount
        ),
        notificationId: Int = BADGE_NOTIFICATION_ID
    ) {
        if (unreadCount <= 0) {
            // 0 表示清除通知與 Badge
            notificationManager.cancel(notificationId)
            return
        }

        val notEmptyTitle = if (title.isNullOrEmpty()) {
            context.getString(R.string.notification_badge_title)
        } else title
        val notEmptyMsg = if (msg.isNullOrEmpty()) {
            String.format(
                Locale.getDefault(),
                context.getString(R.string.notification_badge_message),
                unreadCount
            )
        } else msg
        val notification =
            buildBadgeNotification(pendingIntent, unreadCount, notEmptyTitle, notEmptyMsg)

        // Android 沒有官方 API 判斷通知是否被滑掉
        // 所以這裡直接使用相同 ID notify：
        // - 如果通知還在 → 更新數字
        // - 如果通知被滑掉 → 系統會重新建立通知
        notificationManager.notify(notificationId, notification)
    }

    /**
     * 建立 Notification 並帶上數字
     */
    private fun buildBadgeNotification(
        pendingIntent: PendingIntent,
        number: Int,
        title: String,
        msg: String
    ): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FOR_BADGE)
            .setSmallIcon(R.drawable.ic_firebase)
            .setContentTitle(title)
            .setContentText(msg)
            .setSilent(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .setVibrate(null)

        if (number >= 0) {
            builder.setNumber(number) // 設定 badge 數字
        }

        return builder.build()
    }

    fun testFcmNotification() {
        val testTitle = "TeamWalk"
        val testMsg = "推播"

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("url", "my_coins")
            putExtra("type", "F")
            putExtra("title", testTitle)
            putExtra("msg", testMsg)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val notificationId = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        sendFcmNotification(
            notificationId = notificationId,
            title = testTitle,
            message = testMsg,
            pendingIntent = pendingIntent
        )
    }

    fun sendFcmNotification(
        notificationId: Int,
        title: String?,
        message: String?,
        pendingIntent: PendingIntent
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_firebase)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setNumber(0)
            .setAutoCancel(true)
            .setVibrate(LongArray(0))

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
