package com.taiwanlife.teamwalk

object Config {
    const val API_CODE_SUCCESS = "0000"
    const val API_BODY_EMPTY_MESSAGE = "Response body is null"
    // 沒有帶Token API會給這個code
    const val API_CODE_NO_TOKEN = "500"
    // 除了這隻以外的path都需要戴上JWT token
    const val API_LOGIN_PATH = "login"

    const val FITBIT_BASE_URL = "https://api.fitbit.com/oauth2/"
    const val GARMIN_BASE_URL = "https://connectapi.garmin.com/oauth-service/oauth/"

    const val API_SYS_ID = "teamwalk"
    const val API_CSSO_HEADER_LOCATION = "location"
    const val JAVASCRIPT_BRIDGE_NAME = "JSBridge"

    const val GOOGLE_HEALTH_CONNECT_PACKAGE_NAME = "com.google.android.apps.healthdata"

    // region 自訂義事件參數
    const val EVENT_GARMIN_CONNECT_DONE = "EVENT_GARMIN_CONNECT_DONE"
    const val EVENT_FITBIT_CONNECT_DONE = "EVENT_FITBIT_CONNECT_DONE"

    const val EVENT_NO_TOKEN_TO_LOGIN = "EVENT_NO_TOKEN_TO_LOGIN"

    const val EVENT_EXECUTE_JAVASCRIPT_CALLBACK = "EVENT_EXECUTE_JAVASCRIPT_CALLBACK"
    //end region

    // region 推播設定參數
    const val CHANNEL_ID = "teamwalk"
    const val CHANNEL_NAME = "Teamwalk 推播"
    const val CHANNEL_DESCRIPTION = "用來傳遞 Teamwalk 推播"
    const val BADGE_NOTIFICATION_ID = 1001
    // end region

    // region secured preference 使用的Key值
    const val PREF_LOGIN = "com.taiwanlife.teamwalk.android.pref_login"

    const val PREF_LOGIN_SEGMENT_CONTROL_POS =
        "com.taiwanlife.teamwalk.android.pref_login_segment_control_pos"
    const val PREF_LOGIN_REMEMBER_ME = "com.taiwanlife.teamwalk.android.pref_login_remember_me"
    const val PREF_LOGIN_PID = "com.taiwanlife.teamwalk.android.pref_login_pid"
    const val PREF_LOGIN_PATTERN_STATUS =
        "com.taiwanlife.teamwalk.android.pref_login_pattern_status"
    const val PREF_LOGIN_AUTH = "com.taiwanlife.teamwalk.android.pref_login_auth"
    // 備註: Firebase Installations Unique Id
    const val PREF_LOGIN_FID = "com.taiwanlife.teamwalk.android.pref_login_fid"
    const val KNOWS_ROOT = "com.taiwanlife.teamwalk.android.pref_login_knows_root"

    const val PW_PAGE_FLAG = "Teamwalk_PwPageFlag"

    // 登入資訊
    const val PREF_LOGIN_USERNAME = "com.taiwanlife.teamwalk.android.pref_login_username"
    const val PREF_LOGIN_TICKET = "com.taiwanlife.teamwalk.android.pref_login_ticket"
    const val PREF_LOGIN_TOKEN = "com.taiwanlife.teamwalk.android.pref_login_token"
    const val PREF_LOGIN_REFRESH_TOKEN = "com.taiwanlife.teamwalk.android.pref_login_refresh_token"
    const val PREF_LOGIN_EXP = "com.taiwanlife.teamwalk.android.pref_login_exp"
    const val PREF_LOGIN_CASTGC = "com.taiwanlife.teamwalk.android.pref_login_castgc"
    const val PREF_LOGIN_UUID = "com.taiwanlife.teamwalk.android.pref_login_uuid"

    // 新增的 為了不和上面的搞混 相同功能的也先重新做一個 到時候要清理才能將上面的一次全部刪除
    const val SP_FCM_TOKEN = "SP_FCM_TOKEN"
    const val SP_FIREBASE_INSTALLATIONS_UNIQUE_ID = "SP_FIREBASE_INSTALLATIONS_UNIQUE_ID"

    const val SP_LOGIN_JWT_TOKEN = "SP_LOGIN_JWT_TOKEN"
    const val SP_LOGIN_PID = "SP_LOGIN_PID"
    const val SP_LOGIN_AUTH = "SP_LOGIN_AUTH"
    const val SP_LOGIN_REMEMBER_ME = "SP_LOGIN_REMEMBER_ME"
    const val SP_LOGIN_SEGMENT_CONTROL_POS = "SP_LOGIN_SEGMENT_CONTROL_POS"
    const val SP_SHOW_SECURITY_ALERT_FIRST_TIME = "SP_SHOW_SECURITY_ALERT_FIRST_TIME"

    const val SP_NOTIFICATION = "SP_NOTIFICATION"
    const val SP_BIND_GARMIN = "SP_BIND_GARMIN"
    const val SP_BIND_FITBIT = "SP_BIND_FITBIT"
    const val SP_BIND_CURRENT_DEVICE = "SP_BIND_CURRENT_DEVICE"

    const val SP_USER_INFO = "SP_USER_INFO"
    const val SP_KNOWS_ROOT = "SP_KNOWS_ROOT"
    // end region

    // region 根據不同的Build Types 有不同的環境變數

    // CSSO URL
    val variableCssoUrl = EnvironmentVariable(
        release = "https://csso.taiwanlife.com/csso/",
        debug = "https://cssouat.taiwanlife.com/csso/",
        sit = "https://cssouat.taiwanlife.com/csso/",
        uat = "https://cssouat.taiwanlife.com/csso/"
    )

    // CSSO Forget Password URL
    val variableCssoForgetMimaUrl = EnvironmentVariable(
        release = "https://csso.taiwanlife.com/csso/mobileForget?outsite=teamwalk",
        debug = "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk",
        sit = "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk",
        uat = "https://cssouat.taiwanlife.com/csso/mobileForget?outsite=teamwalk"
    )

    // CSSO Sign Up URL
    val variableCssoSignUpUrl = EnvironmentVariable(
        release = "https://csso.taiwanlife.com/csso/mobileRegister?outsite=teamwalk",
        debug = "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk",
        sit = "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk",
        uat = "https://cssouat.taiwanlife.com/csso/mobileRegister?outsite=teamwalk"
    )

    // TCAV URL
    val variableTcavUrl = EnvironmentVariable(
        release = "https://tcav.taiwanlife.com/",
        debug = "https://tcavuat.taiwanlife.com/",
        sit = "https://tcavuat.taiwanlife.com/",
        uat = "https://tcavuat.taiwanlife.com/"
    )

    // API URL
    val variableApiUrl = EnvironmentVariable(
        release = "https://teamwalk.taiwanlife.com/frontend/api/",
        debug = "https://demo.mutron.com.tw/teamwalk-fe-api/",
        sit = "http://10.1.242.55:9080/frontend/api/",
        uat = "https://demo.mutron.com.tw/teamwalk-fe-api/"
//        uat = "https://teamwalkuat.taiwanlife.com/frontend/api/"
    )

    // Web URL
    val variableWebUrl = EnvironmentVariable(
        release = "https://teamwalk.taiwanlife.com/frontend/",
//        debug = "http://teamwork-frontend.bestamina.net:8080/frontend/",
        debug = "https://demo.mutron.com.tw/teamwalk/bridge",
        sit = "http://10.1.242.55:9080/frontend/",
//        uat = "https://teamwalkuat.taiwanlife.com/frontend/",
        uat = "https://demo.mutron.com.tw/teamwalk/bridge",
    )

    // Origin
    val variableOrigin = EnvironmentVariable(
        release = "https://teamwalk.taiwanlife.com",
        debug = "http://teamwork-frontend.bestamina.net:8080",
        sit = "http://10.1.242.55:9080",
        uat = "https://teamwalkuat.taiwanlife.com"
    )

    // Taiwanlife Member URL
    val variableTaiwanlifeMemberUrl = EnvironmentVariable(
        release = "https://www.taiwanlife.com/member?service=https%3A%2F%2Ftcav.taiwanlife.com%2Flogin",
        debug = "https://uatnew.taiwanlife.com/member?service=https:%2F%2Ftcavuat.taiwanlife.com%2Flogin",
        sit = "https://uatnew.taiwanlife.com/member?service=https:%2F%2Ftcavuat.taiwanlife.com%2Flogin",
        uat = "https://uatnew.taiwanlife.com/member?service=https:%2F%2Ftcavuat.taiwanlife.com%2Flogin"
    )

    // Google Client ID
    val variableConnectGoogleClientId = EnvironmentVariable(
        release = "29548848966-igadugv3n8h8htautt1lfmg5l6kt34vh.apps.googleusercontent.com",
        debug = "773524163063-6je6tkpag1qgcapsn7kuv6s0h85tkct4.apps.googleusercontent.com",
        sit = "773524163063-6je6tkpag1qgcapsn7kuv6s0h85tkct4.apps.googleusercontent.com",
        uat = "29548848966-igadugv3n8h8htautt1lfmg5l6kt34vh.apps.googleusercontent.com"
    )

    // Fitbit Client ID
    val variableConnectFitbitClientId = EnvironmentVariable(
        release = "22BVVM",
        debug = "22BZQG",
        sit = "23B3KR",
        uat = "23B3KR"
    )

    // Fitbit Client Secret
    val variableConnectFitbitClientSecret = EnvironmentVariable(
        release = "00403c68f5025d238d022963dfab43dd",
        debug = "565d1f1a0822d1fbf50cedae0041d3ee",
        sit = "c586771e4f9a4ae17ec0b1de15f38ef6",
        uat = "c586771e4f9a4ae17ec0b1de15f38ef6"
    )

    // Garmin Consumer Key
    val variableConnectGarminConsumerKey = EnvironmentVariable(
        release = "4a08c804-fbd9-41f7-87f7-b7bcd44d430a",
        debug = "fee4d993-db0d-4e55-855f-b7ed099ce358",
        sit = "0b3e7e2c-328a-4bc6-b713-1dd7774ff3d8",
        uat = "0b3e7e2c-328a-4bc6-b713-1dd7774ff3d8"
    )

    // Garmin Consumer Secret
    val variableConnectGarminConsumerSecret = EnvironmentVariable(
        release = "DJ1wEi3uNk0rbBDdalerp6ZYqmFJ2D4kf4t",
        debug = "4b3u2KpOwZdjymZSW9IqbfKXZ4g9MS4tmQb",
        sit = "l9FrmPYVSNgqTT5SOwPfOuT172Vrtmi2tV0",
        uat = "l9FrmPYVSNgqTT5SOwPfOuT172Vrtmi2tV0"
    )

    // Garmin 網址
    val variableGarminUrl = EnvironmentVariable(
        release = "https://connectapi.garmin.com/oauth-service/oauth/",
        debug = "https://connectapi.garmin.com/oauth-service/oauth/",
        sit = "https://connectapi.garmin.com/oauth-service/oauth/",
        uat = "https://connectapi.garmin.com/oauth-service/oauth/"
    )

    // Google fitbit 網址
    val variableFitbitUrl = EnvironmentVariable(
        release = "https://api.fitbit.com/oauth2/",
        debug = "https://api.fitbit.com/oauth2/",
        sit = "https://api.fitbit.com/oauth2/",
        uat = "https://api.fitbit.com/oauth2/"
    )
    //endregion
}