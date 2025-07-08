package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class IndexStepInfo(
    /**
     * 目標步數
     */
    @SerializedName("steps_goal")
    val stepsGoal: Int,
    /**
     * 今日步數
     */
    @SerializedName("steps_today")
    val stepsToday: Int,
    /**
     * 步行距離(公里)
     */
    @SerializedName("steps_distance")
    val stepsDistance: Int,
    /**
     * 消耗的卡路里
     */
    @SerializedName("calories")
    val calories: Int
)