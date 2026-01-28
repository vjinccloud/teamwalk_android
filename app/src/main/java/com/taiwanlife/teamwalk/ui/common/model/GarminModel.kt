package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

/**
 * 拿來送到JS介面的物件
 */
data class GarminModel(
    @SerializedName("oauthToken")
    val oauthToken: String,
    @SerializedName("oauthTokenSecret")
    val oauthTokenSecret: String,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("jti")
    val jti: String,
)