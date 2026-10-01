package com.taiwanlife.teamwalk

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.taiwanlife.teamwalk.Config.CHANNEL_DESCRIPTION
import com.taiwanlife.teamwalk.Config.CHANNEL_DESCRIPTION_FOR_BADGE
import com.taiwanlife.teamwalk.Config.CHANNEL_ID
import com.taiwanlife.teamwalk.Config.CHANNEL_ID_FOR_BADGE
import com.taiwanlife.teamwalk.Config.CHANNEL_NAME
import com.taiwanlife.teamwalk.Config.CHANNEL_NAME_FOR_BADGE
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.taiwanlife.teamwalk.di.appModule
import com.taiwanlife.teamwalk.utils.AppUuidManager
import com.taiwanlife.teamwalk.utils.CrashlyticsTree
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.WafManager
import org.koin.core.context.startKoin
import timber.log.Timber

class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        // 目前前景的 Activity，供 WafManager 把停機頁開在同一個 task 上面（回前景時才不會被主畫面蓋掉）
        @SuppressLint("StaticFieldLeak")
        @Volatile
        var currentActivity: Activity? = null
            private set

        fun createNotificationChannel(context: Context = MyApplication.context) {
            // 從 Android 8.0 (API Level 26) 開始，才需要 Notification Channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 設定通知的重要性
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
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

        if (BuildConfig.ENABLE_API_LOG) {
            Timber.plant(Timber.DebugTree())
        }

        // 刻意不跟 DebugTree 一樣綁 ENABLE_API_LOG：那個旗標在 release 是 false，
        // 綁上去等於正式機完全沒接 Crashlytics。
        if (BuildConfig.ENABLE_CRASHLYTICS) {
            initCrashlytics()
            Timber.plant(CrashlyticsTree())
        }

        val koinApp = startKoin {
            modules(appModule)
        }

//        SecuredPreferenceStore.init(this, DefaultRecoveryHandler())
        SecuredPreferenceStoreManager.init(this)
        createNotificationChannel(this)

        // 追蹤目前前景 Activity（WafManager 用來把停機頁開在同一個 task）
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) { currentActivity = activity }
            override fun onActivityPaused(activity: Activity) {
                if (currentActivity === activity) currentActivity = null
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        // 0003279 WAF 停機檢查：開 App(冷啟第一次 ON_START) 與 背景回前景 都會觸發，不分登入狀態
        val wafManager = koinApp.koin.get<WafManager>()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                wafManager.check()
            }
        })
    }

    /**
     * 用 appUuid 當 Crashlytics 的使用者識別，不用會員帳號。
     *
     * appUuid 是既有的 [AppUuidManager] 產的 32 碼 hex，登入時本來就會送給後端，
     * 所以出事要對照回是哪個使用者由後端查即可，Firebase 上不必留個資。
     */
    private fun initCrashlytics() {
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            setUserId(AppUuidManager.getOrCreate())
            setCustomKey("env", BuildConfig.BUILD_TYPE)
            setCustomKey("version", "${BuildConfig.VERSION_NAME} (vc${BuildConfig.VERSION_CODE})")
            setCustomKey("build", BuildConfig.GIT_HASH)
            setCustomKey("api_url", getString(R.string.api_url))
        }
    }
}