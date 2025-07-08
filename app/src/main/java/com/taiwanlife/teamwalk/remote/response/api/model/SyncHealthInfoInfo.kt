package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class SyncHealthInfoInfo(
    /**
     * 日期
     */
    @SerializedName("date")
    val date: String,
    /**
     * 資料
     */
    @SerializedName("data")
    val data: Int
)