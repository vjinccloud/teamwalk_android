package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.ProductInfo
import com.taiwanlife.teamwalk.remote.response.api.model.SingUpInfo
import com.taiwanlife.teamwalk.remote.response.api.model.TaskInfo

data class GetMyInfoResponse(
    /**
     * 連續簽到設定
     */
    @SerializedName("sign_up")
    val signUp: List<SingUpInfo>,
    /**
     * 任務
     */
    @SerializedName("tasks")
    val tasks: List<TaskInfo>,
    /**
     * 商城
     */
    @SerializedName("product")
    val product: List<ProductInfo>
)