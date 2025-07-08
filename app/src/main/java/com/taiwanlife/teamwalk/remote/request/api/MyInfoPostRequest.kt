package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class MyInfoPostRequest(
    /**
     * 行動電話
     */
    @SerializedName("mobile")
    val mobile: String?,
    /**
     * E-Mail
     */
    @SerializedName("email")
    val email: String?,
    /**
     * 縣市
     */
    @SerializedName("county")
    val county: String?,
    /**
     * 鄉鎮市區
     */
    @SerializedName("district")
    val district: String?,
    /**
     * 暱稱
     */
    @SerializedName("nickname")
    val nickname: String?,
    /**
     * 會員自行上傳頭像
     */
    @SerializedName("custom_image")
    val customImage: String?,
    /**
     * 會員內建頭像(改成代碼)
     */
    @SerializedName("buildin_image")
    val buildinImage: String?,
    /**
     * 會員內建濾鏡(0~5)
     */
    @SerializedName("filter")
    val filter: String?,
    /**
     * 心情小語
     */
    @SerializedName("mood")
    val mood: String?,
    /**
     * 使用阿龍角色
     */
    @SerializedName("badge_role_id")
    val badgeRoleId: String?
) : BaseRequest()