package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class WebCommand(
    @SerializedName("action") val action: String,
    @SerializedName("status") val status: String?,
    @SerializedName("url") val url: String?
)