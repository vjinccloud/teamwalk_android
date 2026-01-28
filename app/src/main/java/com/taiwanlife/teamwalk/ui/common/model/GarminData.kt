package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class GarminData(
    @SerializedName("tsGarmin")
    val tsGarmin: String = "",
    @SerializedName("oauthToken")
    val oauthToken: String = "",
    @SerializedName("oauthTokenSecret")
    val oauthTokenSecret: String = "",
    @SerializedName("accessToken")
    val accessToken: String = "",
    @SerializedName("refreshToken")
    val refreshToken: String = "",
    @SerializedName("jti")
    val jti: String = "",
)