package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName


data class ShareContentModel(
    @SerializedName("status")
    val status: String,
    @SerializedName("msg")
    val message: String
)