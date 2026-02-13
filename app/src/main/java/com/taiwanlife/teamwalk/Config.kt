package com.taiwanlife.teamwalk

import java.time.ZoneId
import java.util.Locale

object Config {
    // 除了正式版不顯示 額外在新增一個個參數控制是否顯示Toast
    const val SHOW_DEBUG_TOAST = false
    val TAIWAN_ZONE_ID = ZoneId.of("Asia/Taipei")
    val TAIWAN_LOCALE = Locale.TAIWAN

    const val API_CODE_SUCCESS = "0000"
    const val API_BODY_EMPTY_MESSAGE = "Response body is null"

    // 沒有帶Token API會給這個code
    const val API_CODE_500_LOG_OUT = "500"
    const val API_CODE_401_LOG_OUT = "401"

    // 除了這隻以外的path都需要戴上JWT token
    const val API_LOGIN_PATH = "login"

    const val JAVASCRIPT_BRIDGE_NAME = "JSBridge"
    const val WEBVIEW_CALLBACK_SCHEME = "teamwalk"

    const val GOOGLE_HEALTH_CONNECT_PACKAGE_NAME = "com.google.android.apps.healthdata"

    // region 自訂義事件參數
    const val EVENT_GARMIN_CONNECT_DONE = "EVENT_GARMIN_CONNECT_DONE"
    const val EVENT_FITBIT_CONNECT_DONE = "EVENT_FITBIT_CONNECT_DONE"

    const val EVENT_NO_ID_TO_LOGIN = "EVENT_NO_ID_TO_LOGIN"

    const val EVENT_EXECUTE_JAVASCRIPT_CALLBACK = "EVENT_EXECUTE_JAVASCRIPT_CALLBACK"

    const val EVENT_ONBOARDING_CONNECT_COMPLETE_REFRESH_HOME = "EVENT_ONBOARDING_CONNECT_COMPLETE_REFRESH_HOME"
    //end region

    // region 推播設定參數
    const val CHANNEL_ID = "teamwalk"
    const val CHANNEL_ID_FOR_BADGE = "teamwalk_badge"
    const val CHANNEL_NAME = "Teamwalk 推播"
    const val CHANNEL_NAME_FOR_BADGE = "Teamwalk 未讀提醒推播"
    const val CHANNEL_DESCRIPTION = "用來傳遞 Teamwalk 推播"
    const val CHANNEL_DESCRIPTION_FOR_BADGE = "用來傳遞 Teamwalk 未讀訊息的推播"
    const val BADGE_NOTIFICATION_ID = 1001
    // end region

    // region secured preference 使用的Key值
    // 新增的 相同功能的也先重新做一個 到時候要清理才能將上面的一次全部刪除
    // 不准Token 改名
    const val SP_FCM_IDENTIFIER = "SP_FCM_IDENTIFIER"
    const val SP_FIREBASE_INSTALLATIONS_UNIQUE_ID = "SP_FIREBASE_INSTALLATIONS_UNIQUE_ID"

    const val SP_LOGIN_JWT = "SP_LOGIN_JWT"
    const val SP_LOGIN_REMEMBER_PID = "SP_LOGIN_REMEMBER_PID"
    const val SP_LOGIN_AUTH = "SP_LOGIN_AUTH"
    const val SP_LOGIN_REMEMBER_ME = "SP_LOGIN_REMEMBER_ME"
    const val SP_LOGIN_SEGMENT_CONTROL_POS = "SP_LOGIN_SEGMENT_CONTROL_POS"
    const val SP_SHOW_SECURITY_ALERT_FIRST_TIME = "SP_SHOW_SECURITY_ALERT_FIRST_TIME"

    const val SP_NOTIFICATION = "SP_NOTIFICATION"
    const val SP_BIND_GARMIN = "SP_BIND_GARMIN"
    const val SP_BIND_FITBIT = "SP_BIND_FITBIT"
    const val SP_BIND_CURRENT_DEVICE = "SP_BIND_CURRENT_DEVICE"

    const val SP_USER_INFO = "SP_USER_INFO"
    const val SP_GARMIN_VERIFIER = "SP_USER_INFO"
    const val SP_GARMIN_STATE = "SP_GARMIN_STATE"
    const val SP_BINDING_FROM_ONBOARD = "SP_BINDING_FROM_ONBOARD"
//    const val SP_KNOWS_ROOT = "SP_KNOWS_ROOT"

    // 在Cookie內部 用來跟CSSO使用
    const val SP_CASTGC = "SP_CASTGC"
    const val SP_PID = "SP_PID"
    // end region

    // region 推播相關
    const val NOTIFICATION_KEY_URL = "url"
    const val NOTIFICATION_KEY_TITLE = "title"
    const val NOTIFICATION_KEY_MSG = "msg"
    const val NOTIFICATION_KEY_TYPE = "type"
    const val NOTIFICATION_KEY_BADGE = "badge"

    enum class NotificationType(val v: String) {
        NONE("N"),
        URL("L"),
        APP_PAGE("F")
    }

    data class NotificationAppPageData(
        val urlValue: String,
        val realPath: String,
    )

    // 目前收到NONE時其實也要導頁 到我的的通知頁面
    val noneAppPageData = NotificationAppPageData("", "my/notification/list")
    val listOfNotificationAppPageData = listOf(
        NotificationAppPageData("homepage", "main"),
        NotificationAppPageData("team_challenge", "challenge/team_pk_board"),
        NotificationAppPageData("my_coins", "my/coin/list"),
        NotificationAppPageData("coins_store", "my/product/main"),
    )
    // end region
}