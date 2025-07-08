package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class MainHealthStepInfo(
    /**
     * */
    @SerializedName("week")
    val week: MainHealthStepDetailInfo,
    /**
     * */
    @SerializedName("month")
    val month: MainHealthStepDetailInfo
)