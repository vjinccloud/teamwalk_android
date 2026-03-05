package com.taiwanlife.teamwalk.ui.login

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class LogRequest(
    @SerializedName("appl_id")
    val applId: String,
    @SerializedName("ticket")
    val ticket: String,
    @SerializedName("service")
    val service: String,
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("is_remember_me")
    val isRememberMe: Boolean
)