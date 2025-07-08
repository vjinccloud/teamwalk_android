package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class SingUpInfo(
    /**
     * 獎勵龍珠
     */
    @SerializedName("coins")
    val coins: Int,
    /**
     * 龍珠發畢後之經驗值獎勵
     */
    @SerializedName("exp")
    val exp: Int,
    /**
     * 獎勵型態(COIN：龍珠、EXP：經驗值)
     */
    @SerializedName("bonus_type")
    val bonusType: String
)