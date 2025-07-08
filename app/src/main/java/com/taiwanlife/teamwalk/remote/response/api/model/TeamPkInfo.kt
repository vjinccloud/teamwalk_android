package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TeamPkInfo(
    /**
     * 團體PK賽ID
     */
    @SerializedName("team_act_id")
    val teamActId: String,
    /**
     * 活動名稱
     */
    @SerializedName("team_act_name")
    val teamActName: String,
    /**
     * 活動Banner
     */
    @SerializedName("banner")
    val banner: String?,
    /**
     * 活動報名截止日期
     */
    @SerializedName("activity_deadline")
    val activityDeadline: String,
    /**
     * 參與人數
     */
    @SerializedName("participent_limit")
    val participentLimit: Int,
    /**
     * 總獎勵龍珠數
     */
    @SerializedName("total_coins")
    val totalCoins: Int,
    /**
     * */
    @SerializedName("recent_join")
    val recentJoin: List<String>?,
    /**
     * 狀態(PENDING:即將開賽,ING:進行中,C:結算中,E:已結束)
     */
    @SerializedName("status")
    val status: String,
    /**
     * 比賽結束日期(當status為ING才有值)
     */
    @SerializedName("game_end_date")
    val gameEndDate: String?,
    /**
     * 邀請挑戰幾天後失效(當status為C才會有值)
     */
    @SerializedName("invite_expire_day")
    val inviteExpireDay: Int?
)