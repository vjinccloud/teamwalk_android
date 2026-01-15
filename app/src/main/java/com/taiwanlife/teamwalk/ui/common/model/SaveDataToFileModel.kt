package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName


data class SaveDataToFileModel(
    @SerializedName("data")
    val data: String,
    @SerializedName("fileName")
    val fileName: String
)