package com.taiwanlife.teamwalk

data class EnvironmentConfig(
    val networkSecurityConfigResId: String, // R.xml.network_security_config_release 或 R.xml.network_security_config_debug
    val cssoUrl: String,
    val cssoForgetMimaUrl: String,
    val cssoSignUpUrl: String,
    val tcavUrl: String,
    val apiUrl: String,
    val webUrl: String,
    val origin: String,
    val taiwanlifeMemberUrl: String,
    val connectGoogleClientId: String,
    val connectFitbitClientId: String,
    val connectFitbitClientSecret: String,
    val connectGarminConsumerKey: String,
    val connectGarminConsumerSecret: String,

    // 新增環境參數
    val garminUrl: String,
    val googleFitbitUrl: String,
)

data class EnvironmentVariable(
    val release: String,
    val debug: String,
    val sit: String,
    val uat: String
)