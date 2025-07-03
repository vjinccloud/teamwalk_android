package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName


data class Competition(
    @SerializedName("pk_id")
    val pkId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("banner")
    val banner: String,
    @SerializedName("activity_deadline")
    val activityDeadline: String,
    @SerializedName("game_start_date")
    val gameStartDate: String,
    @SerializedName("game_end_date")
    val gameEndDate: String,
    @SerializedName("participent_limit")
    val participantLimit: Int,
    @SerializedName("total_coins")
    val totalCoins: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("recent_join")
    val recentJoin: List<String>
)
