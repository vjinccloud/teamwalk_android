package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class SyncHealthDataModel(
    @SerializedName("step")
    val step: List<TeamWalkRecordModel>,

    @SerializedName("sleep")
    val sleep: List<TeamWalkRecordModel>
)
