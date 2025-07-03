package com.taiwanlife.teamwalk

import com.taiwanlife.teamwalk.Config.variableApiUrl
import com.taiwanlife.teamwalk.Config.variableConnectFitbitClientId
import com.taiwanlife.teamwalk.Config.variableConnectFitbitClientSecret
import com.taiwanlife.teamwalk.Config.variableConnectGarminConsumerKey
import com.taiwanlife.teamwalk.Config.variableConnectGarminConsumerSecret
import com.taiwanlife.teamwalk.Config.variableConnectGoogleClientId
import com.taiwanlife.teamwalk.Config.variableCssoForgetMimaUrl
import com.taiwanlife.teamwalk.Config.variableCssoSignUpUrl
import com.taiwanlife.teamwalk.Config.variableCssoUrl
import com.taiwanlife.teamwalk.Config.variableFitbitUrl
import com.taiwanlife.teamwalk.Config.variableGarminUrl
import com.taiwanlife.teamwalk.Config.variableNetworkSecurityConfigResId
import com.taiwanlife.teamwalk.Config.variableOrigin
import com.taiwanlife.teamwalk.Config.variableTaiwanlifeMemberUrl
import com.taiwanlife.teamwalk.Config.variableTcavUrl
import com.taiwanlife.teamwalk.Config.variableWebUrl

/**
 * 用來管理對應不同BuildConfig下的參數
 */
object EnvironmentManager {

    fun getEnvironmentConfig(): EnvironmentConfig {
        return when (BuildConfig.BUILD_TYPE) {
            "debug" -> DEBUG_ENVIRONMENT_CONFIG
            "sit" -> SIT_ENVIRONMENT_CONFIG
            "uat" -> UAT_ENVIRONMENT_CONFIG
            "release" -> RELEASE_ENVIRONMENT_CONFIG
            else -> RELEASE_ENVIRONMENT_CONFIG
        }
    }

    val RELEASE_ENVIRONMENT_CONFIG = EnvironmentConfig(
        networkSecurityConfigResId = variableNetworkSecurityConfigResId.release,
        cssoUrl = variableCssoUrl.release,
        cssoForgetMimaUrl = variableCssoForgetMimaUrl.release,
        cssoSignUpUrl = variableCssoSignUpUrl.release,
        tcavUrl = variableTcavUrl.release,
        apiUrl = variableApiUrl.release,
        webUrl = variableWebUrl.release,
        origin = variableOrigin.release,
        taiwanlifeMemberUrl = variableTaiwanlifeMemberUrl.release,
        connectGoogleClientId = variableConnectGoogleClientId.release,
        connectFitbitClientId = variableConnectFitbitClientId.release,
        connectFitbitClientSecret = variableConnectFitbitClientSecret.release,
        connectGarminConsumerKey = variableConnectGarminConsumerKey.release,
        connectGarminConsumerSecret = variableConnectGarminConsumerSecret.release,
        garminUrl = variableGarminUrl.release,
        googleFitbitUrl = variableFitbitUrl.release,
    )

    val DEBUG_ENVIRONMENT_CONFIG = EnvironmentConfig(
        networkSecurityConfigResId = variableNetworkSecurityConfigResId.debug,
        cssoUrl = variableCssoUrl.debug,
        cssoForgetMimaUrl = variableCssoForgetMimaUrl.debug,
        cssoSignUpUrl = variableCssoSignUpUrl.debug,
        tcavUrl = variableTcavUrl.debug,
        apiUrl = variableApiUrl.debug,
        webUrl = variableWebUrl.debug,
        origin = variableOrigin.debug,
        taiwanlifeMemberUrl = variableTaiwanlifeMemberUrl.debug,
        connectGoogleClientId = variableConnectGoogleClientId.debug,
        connectFitbitClientId = variableConnectFitbitClientId.debug,
        connectFitbitClientSecret = variableConnectFitbitClientSecret.debug,
        connectGarminConsumerKey = variableConnectGarminConsumerKey.debug,
        connectGarminConsumerSecret = variableConnectGarminConsumerSecret.debug,
        garminUrl = variableGarminUrl.debug,
        googleFitbitUrl = variableFitbitUrl.debug,
    )

    val SIT_ENVIRONMENT_CONFIG = EnvironmentConfig(
        networkSecurityConfigResId = variableNetworkSecurityConfigResId.sit,
        cssoUrl = variableCssoUrl.sit,
        cssoForgetMimaUrl = variableCssoForgetMimaUrl.sit,
        cssoSignUpUrl = variableCssoSignUpUrl.sit,
        tcavUrl = variableTcavUrl.sit,
        apiUrl = variableApiUrl.sit,
        webUrl = variableWebUrl.sit,
        origin = variableOrigin.sit,
        taiwanlifeMemberUrl = variableTaiwanlifeMemberUrl.sit,
        connectGoogleClientId = variableConnectGoogleClientId.sit,
        connectFitbitClientId = variableConnectFitbitClientId.sit,
        connectFitbitClientSecret = variableConnectFitbitClientSecret.sit,
        connectGarminConsumerKey = variableConnectGarminConsumerKey.sit,
        connectGarminConsumerSecret = variableConnectGarminConsumerSecret.sit,
        garminUrl = variableGarminUrl.sit,
        googleFitbitUrl = variableFitbitUrl.sit,
    )

    val UAT_ENVIRONMENT_CONFIG = EnvironmentConfig(
        networkSecurityConfigResId = variableNetworkSecurityConfigResId.uat,
        cssoUrl = variableCssoUrl.uat,
        cssoForgetMimaUrl = variableCssoForgetMimaUrl.uat,
        cssoSignUpUrl = variableCssoSignUpUrl.uat,
        tcavUrl = variableTcavUrl.uat,
        apiUrl = variableApiUrl.uat,
        webUrl = variableWebUrl.uat,
        origin = variableOrigin.uat,
        taiwanlifeMemberUrl = variableTaiwanlifeMemberUrl.uat,
        connectGoogleClientId = variableConnectGoogleClientId.uat,
        connectFitbitClientId = variableConnectFitbitClientId.uat,
        connectFitbitClientSecret = variableConnectFitbitClientSecret.uat,
        connectGarminConsumerKey = variableConnectGarminConsumerKey.uat,
        connectGarminConsumerSecret = variableConnectGarminConsumerSecret.uat,
        garminUrl = variableGarminUrl.uat,
        googleFitbitUrl = variableFitbitUrl.uat,
    )
}