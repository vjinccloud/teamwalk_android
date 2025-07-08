package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class StepChartInfo(
    /**
     * 日期
     */
    @SerializedName("date")
    val date: String,
    /**
     * 步數
     */
    @SerializedName("step")
    val step: Int
)