package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.MainHealthSleepInfo
import com.taiwanlife.teamwalk.remote.response.api.model.MainHealthStepInfo

data class GetMainHealthInfoResponse(
    /**
     * */
    @SerializedName("step")
    val step: MainHealthStepInfo,
    /**
     * */
    @SerializedName("sleep")
    val sleep: MainHealthSleepInfo
)