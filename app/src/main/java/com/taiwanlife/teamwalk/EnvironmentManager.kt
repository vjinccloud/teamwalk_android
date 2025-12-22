package com.taiwanlife.teamwalk

/**
 * 用來管理對應不同BuildConfig下的參數
 */
object EnvironmentManager {

    fun getEnvironmentConfig(): EnvironmentConfig {
        return environment
//        return when (BuildConfig.BUILD_TYPE) {
//            "debug" -> DEBUG_ENVIRONMENT_CONFIG
//            "sit" -> SIT_ENVIRONMENT_CONFIG
//            "uat" -> UAT_ENVIRONMENT_CONFIG
//            "release" -> RELEASE_ENVIRONMENT_CONFIG
//            else -> RELEASE_ENVIRONMENT_CONFIG
//        }
    }

    val environment = EnvironmentConfig(
        cssoUrl = MyApplication.context.getString(R.string.csso_url),
        cssoForgetMimaUrl = MyApplication.context.getString(R.string.csso_forget_mima_url),
        cssoSignUpUrl = MyApplication.context.getString(R.string.csso_sign_up_url),
        tcavUrl = MyApplication.context.getString(R.string.tcav_url),
        apiUrl = MyApplication.context.getString(R.string.api_url),
        webUrl = MyApplication.context.getString(R.string.web_url),
        origin = MyApplication.context.getString(R.string.origin),
        taiwanlifeMemberUrl = MyApplication.context.getString(R.string.taiwanlife_member_url),
        connectGoogleClientId = MyApplication.context.getString(R.string.connect_google_client_id),
        connectFitbitClientId = MyApplication.context.getString(R.string.connect_fitbit_client_id),
        connectFitbitClientSecret = MyApplication.context.getString(R.string.connect_fitbit_client_s),
        connectGarminConsumerKey = MyApplication.context.getString(R.string.connect_garmin_consumer_key),
        connectGarminConsumerSecret = MyApplication.context.getString(R.string.connect_garmin_consumer_s),
        garminUrl = MyApplication.context.getString(R.string.garmin_url),
        googleFitbitUrl = MyApplication.context.getString(R.string.fitbit_url)
    )
}