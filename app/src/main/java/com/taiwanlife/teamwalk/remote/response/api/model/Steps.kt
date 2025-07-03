package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class Steps(
    @SerializedName("steps_goal")
    val stepsGoal: Int,
    @SerializedName("steps_today")
    val stepsToday: Int,
    @SerializedName("steps_distance")
    val stepsDistance: Int,
    @SerializedName("calories")
    val calories: Int
)