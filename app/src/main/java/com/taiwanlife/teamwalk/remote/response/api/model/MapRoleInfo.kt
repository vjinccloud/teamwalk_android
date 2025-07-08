package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class MapRoleInfo(
    /**
     * 角色ID
     */
    @SerializedName("id")
    val id: String,
    /**
     * 角色名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 角色圖片
     */
    @SerializedName("image")
    val image: String,
    /**
     * 是否完成
     */
    @SerializedName("is_complete")
    val isComplete: String,
    /**
     * 是否使用中
     */
    @SerializedName("is_use")
    val isUse: String
)