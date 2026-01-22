package com.taiwanlife.teamwalk

data class EnvironmentConfig(
    val cssoUrl: String,
    val cssoForgetMimaUrl: String,
    val cssoSignUpUrl: String,
    val tcavUrl: String,
    val apiUrl: String,
    val webUrlBase: String,
    val webUrl: String,
    val origin: String,
    val taiwanlifeMemberUrl: String,
    val connectGoogleClientId: String,
    val connectFitbitClientId: String,
    val connectFitbitClientSecret: String,
    val connectGarminConsumerKey: String,
    val connectGarminConsumerSecret: String,
    val connectGarminPortal: String,

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