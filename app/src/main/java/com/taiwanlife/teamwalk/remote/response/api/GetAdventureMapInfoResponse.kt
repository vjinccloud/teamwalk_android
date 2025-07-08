package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class GetAdventureMapInfoResponse(
    /**
     * 登入者角色在這張地圖的經值驗起點
     */
    @SerializedName("exp_start")
    val expStart: Int,
    /**
     * 目前地圖最大經驗值
     */
    @SerializedName("exp_end")
    val expEnd: Int,
    /**
     * 目前地圖進到哪個階段
     */
    @SerializedName("checkpoint_level")
    val checkpointLevel: Int,
    /**
     * 是否完成
     */
    @SerializedName("is_complete")
    val isComplete: String
)