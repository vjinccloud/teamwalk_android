package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class InsuranceCardInfo(
    /**
     * 保險卡片主旨
     */
    @SerializedName("subject")
    val subject: String,
    /**
     * 保險卡片Url
     */
    @SerializedName("url")
    val url: String,
    /**
     * 保險卡片路徑
     */
    @SerializedName("image")
    val image: String
)