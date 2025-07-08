package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class InsurancePopularActInfo(
    /**
     * 熱門活動主旨
     */
    @SerializedName("subject")
    val subject: String,
    /**
     * 熱門活動內容
     */
    @SerializedName("content")
    val content: String,
    /**
     * 熱門活動 URL
     */
    @SerializedName("url")
    val url: String,
    /**
     * 熱門活動圖片路徑
     */
    @SerializedName("image")
    val image: String
)