package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class ExpCoinHistoryInfoResponse(
    /**
     * 原因
     */
    @SerializedName("reason")
    val reason: String,
    /**
     * 時間
     */
    @SerializedName("date")
    val date: String,
    /**
     * 類型(G:獲得、U:使用)
     */
    @SerializedName("type")
    val type: String,
    /**
     * 數量
     */
    @SerializedName("amt")
    val amt: Int
)