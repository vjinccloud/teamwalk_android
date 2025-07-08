package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class MainHealthStepDetailInfo(
    /**
     * 總步數
     */
    @SerializedName("total_step")
    val totalStep: Int,
    /**
     * 總卡路里
     */
    @SerializedName("total_calories")
    val totalCalories: Int,
    /**
     * 總距離
     */
    @SerializedName("total_distance")
    val totalDistance: Int,
    /**
     * 過去平均步數
     */
    @SerializedName("pre_avg_step")
    val preAvgStep: Int,
    /**
     * 本期間與上期間的是增加還減少(UP / DOWN)
     */
    @SerializedName("pre_avg_step_direction")
    val preAvgStepDirection: String,
    /**
     * 單日最多步
     */
    @SerializedName("one_day_max_step")
    val oneDayMaxStep: Int,
    /**
     * 單日最多日期
     */
    @SerializedName("one_day_max_date")
    val oneDayMaxDate: String,
    /**
     * 達標次數
     */
    @SerializedName("achieve_count")
    val achieveCount: Int,
    /**
     * 圖表資料
     */
    @SerializedName("chart")
    val chart: List<StepChartInfo>
)