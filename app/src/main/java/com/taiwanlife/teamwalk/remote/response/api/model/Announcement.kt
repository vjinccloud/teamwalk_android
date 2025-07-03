package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class Announcement(
    @SerializedName("announcement_id")
    val announcementId: String,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("content")
    val content: String
)