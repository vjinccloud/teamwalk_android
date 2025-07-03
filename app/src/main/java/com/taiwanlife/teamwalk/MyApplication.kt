package com.taiwanlife.teamwalk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.taiwanlife.teamwalk.Config.CHANNEL_DESCRIPTION
import com.taiwanlife.teamwalk.Config.CHANNEL_ID
import com.taiwanlife.teamwalk.Config.CHANNEL_NAME
import com.taiwanlife.teamwalk.di.appModule
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import org.koin.core.context.startKoin
import timber.log.Timber

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            modules(appModule)
        }

        SecuredPreferenceStore.init(this, DefaultRecoveryHandler())
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
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

        // 取得 NotificationManager 實例
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // 註冊通知 Channel
        notificationManager.createNotificationChannel(channel)
    }
}