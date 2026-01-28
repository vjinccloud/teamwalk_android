package com.taiwanlife.teamwalk.remote.response.garmin

import com.google.gson.annotations.SerializedName

data class GarminAccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("scope")
    val scope: String,

    @SerializedName("jti")
    val jti: String
)