package com.taiwanlife.teamwalk

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.taiwanlife.teamwalk.Config.CHANNEL_DESCRIPTION
import com.taiwanlife.teamwalk.Config.CHANNEL_DESCRIPTION_FOR_BADGE
import com.taiwanlife.teamwalk.Config.CHANNEL_ID
import com.taiwanlife.teamwalk.Config.CHANNEL_ID_FOR_BADGE
import com.taiwanlife.teamwalk.Config.CHANNEL_NAME
import com.taiwanlife.teamwalk.Config.CHANNEL_NAME_FOR_BADGE
import com.taiwanlife.teamwalk.di.appModule
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import org.koin.core.context.startKoin

class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        fun createNotificationChannel(context: Context = MyApplication.context) {
            // 從 Android 8.0 (API Level 26) 開始，才需要 Notification Channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 設定通知的重要性
            ).apply {
                description = CHANNEL_DESCRIPTION
                // 其他可選設定：
                // enableLights(true) // 是否開啟指示燈
                // lightColor = Color.RED // 指示燈顏色
                // enableVibration(true) // 是否震動
                // vibrationPattern = longArrayOf(100, 200, 300, 400, 500) // 自定義震動模式
            }
            val channelForUnread = NotificationChannel(
                CHANNEL_ID_FOR_BADGE,
                CHANNEL_NAME_FOR_BADGE,
                NotificationManager.IMPORTANCE_HIGH // 設定通知的重要性
            ).apply {
                description = CHANNEL_DESCRIPTION_FOR_BADGE
                importance = NotificationManager.IMPORTANCE_MIN
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                setShowBadge(true)
                // 其他可選設定：
                // enableLights(true) // 是否開啟指示燈
                // lightColor = Color.RED // 指示燈顏色
                // enableVibration(true) // 是否震動
                // vibrationPattern = longArrayOf(100, 200, 300, 400, 500) // 自定義震動模式
            }

            // 取得 NotificationManager 實例
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // 註冊通知 Channel
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(channelForUnread)
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        if (BuildConfig.DEBUG) {
//            FileLoggingTree.clearLogs(this)
//            Timber.plant(Timber.DebugTree())
//            Timber.plant(FileLoggingTree(this))
        }

        startKoin {
            modules(appModule)
        }

//        SecuredPreferenceStore.init(this, DefaultRecoveryHandler())
        SecuredPreferenceStoreManager.init(this)
        createNotificationChannel(this)
    }
}