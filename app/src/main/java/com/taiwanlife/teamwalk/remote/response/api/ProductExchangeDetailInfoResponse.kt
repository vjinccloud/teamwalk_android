package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class ProductExchangeDetailInfoResponse(
    /**
     * 兌換代碼
     */
    @SerializedName("exchange_id")
    val exchangeId: String,
    /**
     * 兌換卷名稱
     */
    @SerializedName("exchange_name")
    val exchangeName: String,
    /**
     * 兌換卷圖檔
     */
    @SerializedName("exchange_img")
    val exchangeImg: String,
    /**
     * 使用期限
     */
    @SerializedName("exchange_date")
    val exchangeDate: String,
    /**
     * 狀態(N:未使用、U:已使用、E:已過期)
     */
    @SerializedName("status")
    val status: String
)