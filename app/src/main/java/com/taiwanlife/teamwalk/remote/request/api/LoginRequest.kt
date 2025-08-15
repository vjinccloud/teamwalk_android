package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class LoginRequest(
    /**
     * 登入者帳號
     */
    @SerializedName("appl_id")
    val applId: String,
    /**
     * CSSO Ticket資訊
     */
    @SerializedName("ticket")
    val ticket: String,
    /**
     * CSSO service
     */
    @SerializedName("service")
    val service: String,
    /**
     * app安裝後產生唯一識別碼
     */
    @SerializedName("app_uuid")
    val appUuid: String,
    /**
     * 裝置識別碼
     */
    @SerializedName("device_id")
    val deviceId: String,
    /**
     * FCM推播Token
     */
    @SerializedName("push_id")
    val pushId: String
) : BaseRequest()