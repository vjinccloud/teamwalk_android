package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TaskInfo(
    /**
     * 任務ID
     */
    @SerializedName("task_id")
    val taskId: String,
    /**
     * 任務類型(F：新手、A：進階)
     */
    @SerializedName("task_type")
    val taskType: String,
    /**
     * 任務名稱
     */
    @SerializedName("task_name")
    val taskName: String,
    /**
     * 獎勵龍珠
     */
    @SerializedName("coins")
    val coins: Int,
    /**
     * 經驗值獎勵
     */
    @SerializedName("exp")
    val exp: Int,
    /**
     * 獎勵免費遊戲次數
     */
    @SerializedName("game")
    val game: Int,
    /**
     * 完成百分比
     */
    @SerializedName("complete_rate")
    val completeRate: Int,
    /**
     * 是否啟用
     */
    @SerializedName("is_enable")
    val isEnable: String,
    /**
     * 任務達成狀態(P:進行中、U:已完成待領取、A:已領取)
     */
    @SerializedName("status")
    val status: String
)