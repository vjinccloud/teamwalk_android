package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    /**
     * 登入者token
     */
    @SerializedName("token")
    val token: String,
)
