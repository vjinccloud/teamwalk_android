package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class TeamWalkRecordModel(
    @SerializedName("start_timestamp")
    val startTimestamp: String,

    @SerializedName("end_timestamp")
    val endTimestamp: String,

    @SerializedName("utc_date")
    val utcDate: String,

    @SerializedName("local_date")
    val localDate: String,

    @SerializedName("data")
    val data: Long
)