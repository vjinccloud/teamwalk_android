package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.TeamPkMapInfo
import com.taiwanlife.teamwalk.remote.response.api.model.TeamPkPersonalLeaderBoardInfo
import com.taiwanlife.teamwalk.remote.response.api.model.TeamPkTeamLeaderBoardInfo

data class GetTeamPkInfoResponse(
    /**
     * 活動名稱
     */
    @SerializedName("team_act_name")
    val teamActName: String,
    /**
     * 活動Banner
     */
    @SerializedName("banner")
    val banner: String,
    /**
     * 季節背景(春天:spring夏天:summer秋天:fall冬天:winter)
     */
    @SerializedName("season")
    val season: String,
    /**
     * 活動報名截止日期
     */
    @SerializedName("activity_deadline")
    val activityDeadline: String,
    /**
     * 狀態(PENDING:即將開賽,ING:進行中,C:結算中,E:已結束)
     */
    @SerializedName("status")
    val status: String,
    /**
     * 活動說明活動說明活動說明
     */
    @SerializedName("description")
    val description: String,
    /**
     * 比賽開始日期
     */
    @SerializedName("game_start_date")
    val gameStartDate: String,
    /**
     * 比賽結束日期
     */
    @SerializedName("game_end_date")
    val gameEndDate: String,
    /**
     * 總人數
     */
    @SerializedName("total_people")
    val totalPeople: Int,
    /**
     * 總隊伍
     */
    @SerializedName("total_team")
    val totalTeam: Int,
    /**
     * */
    @SerializedName("recent_join")
    val recentJoin: List<String>,
    /**
     * 目標步數
     */
    @SerializedName("target_steps")
    val targetSteps: Int,
    /**
     * 龍珠總數
     */
    @SerializedName("total_coins")
    val totalCoins: Int,
    /**
     * 團體賽第一名占比
     */
    @SerializedName("bonus_team_no_1")
    val bonusTeamNo1: Int,
    /**
     * 團體賽第一名可獲得龍珠數
     */
    @SerializedName("bonus_team_no_1_coins")
    val bonusTeamNo1Coins: Int,
    /**
     * 團體賽第二名占比
     */
    @SerializedName("bonus_team_no_2")
    val bonusTeamNo2: Int,
    /**
     * 團體賽第二名可獲得龍珠數
     */
    @SerializedName("bonus_team_no_2_coins")
    val bonusTeamNo2Coins: Int,
    /**
     * 團體賽第三名占比
     */
    @SerializedName("bonus_team_no_3")
    val bonusTeamNo3: Int,
    /**
     * 團體賽第三名可獲得龍珠數
     */
    @SerializedName("bonus_team_no_3_coins")
    val bonusTeamNo3Coins: Int,
    /**
     * 個人第一名占比
     */
    @SerializedName("bonus_personal_no_1")
    val bonusPersonalNo1: Int,
    /**
     * 個人第一名可獲得龍珠數
     */
    @SerializedName("bonus_personal_no_1_coins")
    val bonusPersonalNo1Coins: Int,
    /**
     * 個人第二名占比
     */
    @SerializedName("bonus_personal_no_2")
    val bonusPersonalNo2: Int,
    /**
     * 個人第二名可獲得龍珠數
     */
    @SerializedName("bonus_personal_no_2_coins")
    val bonusPersonalNo2Coins: Int,
    /**
     * 個人第三名占比
     */
    @SerializedName("bonus_personal_no_3")
    val bonusPersonalNo3: Int,
    /**
     * 個人第三名可獲得龍珠數
     */
    @SerializedName("bonus_personal_no_3_coins")
    val bonusPersonalNo3Coins: Int,
    /**
     * */
    @SerializedName("team_map")
    val teamMap: List<TeamPkMapInfo>,
    /**
     * */
    @SerializedName("team_leaderboard")
    val teamLeaderboard: List<TeamPkTeamLeaderBoardInfo>,
    /**
     * */
    @SerializedName("personal_leaderboard")
    val personalLeaderboard: List<TeamPkPersonalLeaderBoardInfo>,
    /**
     * 我的隊伍代碼
     */
    @SerializedName("my_team_id")
    val myTeamId: String
)