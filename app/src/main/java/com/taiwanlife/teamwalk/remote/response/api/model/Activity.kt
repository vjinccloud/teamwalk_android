package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("activity_id")
    val activityId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("sub_name")
    val subName: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("home_img")
    val homeImg: String,
    @SerializedName("banner_img")
    val bannerImg: String,
    @SerializedName("url_type")
    val urlType: String,
    @SerializedName("url")
    val url: String
)
