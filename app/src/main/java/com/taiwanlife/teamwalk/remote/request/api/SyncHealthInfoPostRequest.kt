package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.SyncHealthInfoInfo
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class SyncHealthInfoPostRequest(
    /**
     * */
    @SerializedName("step")
    val step: List<SyncHealthInfoInfo>,
    /**
     * */
    @SerializedName("sleep")
    val sleep: List<SyncHealthInfoInfo>
) : BaseRequest()