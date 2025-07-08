package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class MainHealthSleepDetailInfo(
    /**
     * 平均睡眠時間(時)
     */
    @SerializedName("avg_sleep_hour")
    val avgSleepHour: Int,
    /**
     * 平均深睡眠時間(分)
     */
    @SerializedName("avg_sleep_minute")
    val avgSleepMinute: Int,
    /**
     * 平均深層睡眠時間(時)
     */
    @SerializedName("avg_deep_sleep_hour")
    val avgDeepSleepHour: Int,
    /**
     * 平均深層睡眠時間(分)
     */
    @SerializedName("avg_deep_sleep_minute")
    val avgDeepSleepMinute: Int,
    /**
     * 平均淺層睡眠時間(時)
     */
    @SerializedName("avg_light_sleep_hour")
    val avgLightSleepHour: Int,
    /**
     * 平均淺層睡眠時間(分)
     */
    @SerializedName("avg_light_sleep_minute")
    val avgLightSleepMinute: Int,
    /**
     * 本期間與上期間的是增加還減少還是持平(UP / DOWN / -)
     */
    @SerializedName("pre_avg_step_direction")
    val preAvgStepDirection: String,
    /**
     * 達標次數
     */
    @SerializedName("achieve_count")
    val achieveCount: Int,
    /**
     * 圖表資料
     */
    @SerializedName("chart")
    val chart: List<SleepChartInfo>
)