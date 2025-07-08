package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class MyBadgeInfoResponse(
    /**
     * 徽章
     */
    @SerializedName("badge_id")
    val badgeId: String,
    /**
     * 類型 (H:成就徽章, S:特殊徽章 , I:圖鑑)
     */
    @SerializedName("type")
    val type: String,
    /**
     * 徽章名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 類型是H或S才會有達成階段(0/1/2/3)，0代表沒有達到
     */
    @SerializedName("progress_status")
    val progressStatus: Int
)