package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class IndexSleepInfo(
    /**
     * 每日目標睡眠小時
     */
    @SerializedName("sleep_goal")
    val sleepGoal: Int,
    /**
     * 今日睡眠時間(分鐘)
     */
    @SerializedName("sleep_today")
    val sleepToday: Int,
    /**
     * 今日深眠時間(分鐘)
     */
    @SerializedName("sleep_deep_today")
    val sleepDeepToday: Int,
    /**
     * 今日淺眠時間(分鐘)
     */
    @SerializedName("sleep_shallow_today")
    val sleepShallowToday: Int
)