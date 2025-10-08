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

    /**
     * 更新或建立一個 Badge 通知
     */
    fun updateBadge(unreadCount: Int) {
        if (unreadCount <= 0) {
            // 0 表示清除通知與 Badge
            notificationManager.cancel(BADGE_NOTIFICATION_ID)
            return
        }

        val notification = buildNotification(unreadCount)

        // Android 沒有官方 API 判斷通知是否被滑掉
        // 所以這裡直接使用相同 ID notify：
        // - 如果通知還在 → 更新數字
        // - 如果通知被滑掉 → 系統會重新建立通知
        notificationManager.notify(BADGE_NOTIFICATION_ID, notification)
    }

    /**
     * 建立 Notification 並帶上數字
     */
    private fun buildNotification(unreadCount: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_firebase)
            .setContentTitle("新訊息")
            .setContentText("你有 $unreadCount 則未讀訊息")
            .setNumber(unreadCount) // 關鍵 → 設定 badge 數字
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setAutoCancel(false) // 避免點擊自動消失
            .setOngoing(false) // 使用者可以滑掉
            .build()
    }
}
