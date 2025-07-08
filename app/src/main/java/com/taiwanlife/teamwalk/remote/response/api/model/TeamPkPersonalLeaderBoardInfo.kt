package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TeamPkPersonalLeaderBoardInfo(
    /**
     * 團隊頭像
     */
    @SerializedName("avatar")
    val avatar: String,
    /**
     * 會員名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 推薦碼
     */
    @SerializedName("my_referrer_code")
    val myReferrerCode: String,
    /**
     * 目前總步數
     */
    @SerializedName("step")
    val step: Int
)