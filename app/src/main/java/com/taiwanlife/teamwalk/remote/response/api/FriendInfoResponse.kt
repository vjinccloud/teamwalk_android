package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class FriendInfoResponse(
    /**
     * 好友代碼
     */
    @SerializedName("friend_id")
    val friendId: String,
    /**
     * 好友名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 推薦碼
     */
    @SerializedName("my_referrer_code")
    val myReferrerCode: String,
    /**
     * 等級
     */
    @SerializedName("level")
    val level: Int,
    /**
     * 頭像
     */
    @SerializedName("avatar")
    val avatar: String,
    /**
     * 單日最高步數
     */
    @SerializedName("one_day_max_steps")
    val oneDayMaxSteps: Int,
    /**
     * PK勝率
     */
    @SerializedName("winner_rate")
    val winnerRate: Int
)