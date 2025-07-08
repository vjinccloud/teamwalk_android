package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.ActivityInfo
import com.taiwanlife.teamwalk.remote.response.api.model.AnnouncementInfo
import com.taiwanlife.teamwalk.remote.response.api.model.IndexSleepInfo
import com.taiwanlife.teamwalk.remote.response.api.model.IndexStepInfo

data class GetMainInfoResponse(
    /**
     * 熱門活動
     */
    @SerializedName("activities")
    val activities: List<ActivityInfo>,
    /**
     * 公告訊息
     */
    @SerializedName("announcement")
    val announcement: List<AnnouncementInfo>,
    /**
     * */
    @SerializedName("steps")
    val steps: IndexStepInfo,
    /**
     * */
    @SerializedName("sleep")
    val sleep: IndexSleepInfo
)