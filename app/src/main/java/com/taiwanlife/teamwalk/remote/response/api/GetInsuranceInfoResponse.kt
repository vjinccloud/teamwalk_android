package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.InsuranceCardInfo
import com.taiwanlife.teamwalk.remote.response.api.model.InsurancePopularActInfo

data class GetInsuranceInfoResponse(
    /**
     * 團險保險上方卡片
     */
    @SerializedName("card")
    val card: List<InsuranceCardInfo>,
    /**
     * 下方熱門活動
     */
    @SerializedName("popular_act")
    val popularAct: List<InsurancePopularActInfo>
)