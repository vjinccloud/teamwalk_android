package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class SleepChartInfo(
    /**
     * 日期
     */
    @SerializedName("date")
    val date: String,
    /**
     * 深層小時
     */
    @SerializedName("deep")
    val deep: Int,
    /**
     * 淺層小時
     */
    @SerializedName("light")
    val light: Int,
    /**
     * rem小時
     */
    @SerializedName("rem")
    val rem: Int
)