package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class DeviceInfoModel(
    @SerializedName("appUuid")
    val appUuid: String,
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("pushId")
    val pushId: String
)