package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.MapRoleInfo

data class GetAdventureInfoResponse(
    /**
     * 目前遊戲進度到哪一張大地圖(1~10)
     */
    @SerializedName("current_map_level")
    val currentMapLevel: Int,
    /**
     * 所有角色
     */
    @SerializedName("roles")
    val roles: List<MapRoleInfo>
)