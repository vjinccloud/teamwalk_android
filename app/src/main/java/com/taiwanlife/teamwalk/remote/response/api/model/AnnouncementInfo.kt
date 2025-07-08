package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class AnnouncementInfo(
    /**
     * 公告ID
     */
    @SerializedName("announcement_id")
    val announcementId: String,
    /**
     * 主旨
     */
    @SerializedName("subject")
    val subject: String,
    /**
     * 內容
     */
    @SerializedName("content")
    val content: String
)