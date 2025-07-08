package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class Team(
    /**
     * 勝率欄位1(個人：勝率、團體：前10%幾場)
     */
    @SerializedName("summary_1")
    val summary1: Int,
    /**
     * 勝率欄位2(個人：獲勝場數、團體：排名)
     */
    @SerializedName("summary_2")
    val summary2: Int,
    /**
     * 勝率欄位3(挑戰場數)
     */
    @SerializedName("summary_3")
    val summary3: Int
)