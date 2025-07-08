package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class MainHealthSleepInfo(
    /**
     * */
    @SerializedName("week")
    val week: MainHealthSleepDetailInfo,
    /**
     * */
    @SerializedName("month")
    val month: MainHealthSleepDetailInfo
)