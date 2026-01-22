package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class SysParamInfoResponse(
    /**
     * android強制更版(Y:是/N:否)
     */
    @SerializedName("android_is_forced")
    var androidIsForced: String?,
    /**
     * Android版本
     */
    @SerializedName("force_update_ver_android")
    var forceUpdateVerAndroid: String?,
    /**
     * ios強制更版(Y:是/N:否)
     */
    @SerializedName("ios_is_forced")
    val iosIsForced: String?,
    /**
     * IOS版本IOS
     */
    @SerializedName("force_update_ver_ios")
    val forceUpdateVerIos: String?,
    /**
     * Webview版本
     */
    @SerializedName("force_update_ver_webview")
    val forceUpdateVerWebview: String?,
    /**
     * 同步健康裝置資訊天數
     */
    @SerializedName("sync_health_days")
    val syncHealthDays: String?,
    /**
     * 法律條款
     */
    @SerializedName("legal_terms")
    val legalTerms: String?,
    /**
     * 個人資料運用告知聲明
     */
    @SerializedName("personal_info_term")
    val personalInfoTerm: String?,
    /**
     * 隱私權政策
     */
    @SerializedName("privacy_policy")
    val privacyPolicy: String?,
    /**
     * 使用者活動辦法1標題
     */
    @SerializedName("terms_of_use_title1")
    val termsOfUseTitle1: String?,
    /**
     * 使用者活動辦法2標題
     */
    @SerializedName("terms_of_use_title2")
    val termsOfUseTitle2: String?,
    /**
     * 使用者活動辦法3標題
     */
    @SerializedName("terms_of_use_title3")
    val termsOfUseTitle3: String?,
    /**
     * 使用者活動辦法4標題
     */
    @SerializedName("terms_of_use_title4")
    val termsOfUseTitle4: String?,
    /**
     * 使用者活動辦法5標題
     */
    @SerializedName("terms_of_use_title5")
    val termsOfUseTitle5: String?,
    /**
     * 使用者活動辦法1
     */
    @SerializedName("terms_of_use1")
    val termsOfUse1: String?,
    /**
     * 使用者活動辦法2
     */
    @SerializedName("terms_of_use2")
    val termsOfUse2: String?,
    /**
     * 使用者活動辦法3
     */
    @SerializedName("terms_of_use3")
    val termsOfUse3: String?,
    /**
     * 使用者活動辦法4
     */
    @SerializedName("terms_of_use4")
    val termsOfUse4: String?,
    /**
     * 使用者活動辦法5
     */
    @SerializedName("terms_of_use5")
    val termsOfUse5: String?
)