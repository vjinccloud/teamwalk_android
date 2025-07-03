package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.Activity
import com.taiwanlife.teamwalk.remote.response.api.model.Announcement
import com.taiwanlife.teamwalk.remote.response.api.model.Sleep
import com.taiwanlife.teamwalk.remote.response.api.model.Steps

data class IndexResponse(
    @SerializedName("activities")
    val activities: List<Activity>,
    @SerializedName("announcement")
    val announcement: List<Announcement>,
    @SerializedName("lead_rate")
    val leadRate: Int,
    @SerializedName("steps")
    val steps: Steps,
    @SerializedName("sleep")
    val sleep: Sleep
)