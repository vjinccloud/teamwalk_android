package com.taiwanlife.teamwalk.utils

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.taiwanlife.teamwalk.Config.BADGE_NOTIFICATION_ID
import com.taiwanlife.teamwalk.Config.CHANNEL_ID
import com.taiwanlife.teamwalk.MyApplication
import com.taiwanlife.teamwalk.R

class MyNotificationManager(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        MyApplication.createNotificationChannel()
    }

    fun addNotification(message: String, notificationId: Int = BADGE_NOTIFICATION_ID) {
        val notification = buildNotification(-1, message)
        notificationManager.notify(notificationId, notification)
    }


    /**
     * 更新或建立一個 Badge 通知
     */
    fun updateBadge(unreadCount: Int, notificationId: Int = BADGE_NOTIFICATION_ID) {
        if (unreadCount <= 0) {
            // 0 表示清除通知與 Badge
            notificationManager.cancel(notificationId)
            return
        }

        val notification = buildNotification(unreadCount, "你有 $unreadCount 則未讀訊息")

        // Android 沒有官方 API 判斷通知是否被滑掉
        // 所以這裡直接使用相同 ID notify：
        // - 如果通知還在 → 更新數字
        // - 如果通知被滑掉 → 系統會重新建立通知
        notificationManager.notify(notificationId, notification)
    }

    /**
     * 建立 Notification 並帶上數字
     */
    private fun buildNotification(number: Int, message: String): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_firebase)
            .setContentTitle("新訊息")
            .setContentText(message)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setAutoCancel(false) // 避免點擊自動消失
            .setOngoing(false) // 使用者可以滑掉

        if(number >= 0) {
            builder.setNumber(number) // 設定 badge 數字
        }

        return builder.build()
    }
}
