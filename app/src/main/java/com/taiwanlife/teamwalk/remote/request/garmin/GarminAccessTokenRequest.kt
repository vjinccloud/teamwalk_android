package com.taiwanlife.teamwalk.remote.request.garmin

import com.google.gson.annotations.SerializedName

data class GarminAccessTokenRequest(

    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("client_secret")
    val clientSecret: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("redirect_uri")
    val redirectUri: String,

    @SerializedName("code_verifier")
    val codeVerifier: String,

    @SerializedName("grant_type")
    val grantType: String = "authorization_code",
)