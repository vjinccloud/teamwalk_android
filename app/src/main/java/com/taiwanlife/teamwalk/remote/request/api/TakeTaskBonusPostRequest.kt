package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class TakeTaskBonusPostRequest(
    /**
     * 任務ID
     */
    @SerializedName("task_id")
    val taskId: String
) : BaseRequest()