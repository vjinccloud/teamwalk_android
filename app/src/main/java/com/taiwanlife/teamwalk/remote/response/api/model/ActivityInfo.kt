package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

// Base classes for requests and responses
data class ActivityInfo(
    /**
     * 活動ID
     */
    @SerializedName("activity_id")
    val activityId: String,
    /**
     * 主標題
     */
    @SerializedName("name")
    val name: String,
    /**
     * 副標題
     */
    @SerializedName("sub_name")
    val subName: String,
    /**
     * 活動詳細說明(HTML)
     */
    @SerializedName("content")
    val content: String,
    /**
     * 首頁圖檔
     */
    @SerializedName("home_img")
    val homeImg: String,
    /**
     * 詳情Banner圖檔
     */
    @SerializedName("banner_img")
    val bannerImg: String,
    /**
     * URL跳轉類型(E:外部 / I:內部)
     */
    @SerializedName("url_type")
    val urlType: String,
    /**
     * URL跳轉
     */
    @SerializedName("url")
    val url: String
)