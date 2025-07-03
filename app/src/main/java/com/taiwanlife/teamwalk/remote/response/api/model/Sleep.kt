package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class Sleep(
    @SerializedName("sleep_goal")
    val sleepGoal: Int,
    @SerializedName("sleep_today")
    val sleepToday: Int,
    @SerializedName("sleep_deep_today")
    val sleepDeepToday: Int,
    @SerializedName("sleep_shallow_today")
    val sleepShallowToday: Int
)