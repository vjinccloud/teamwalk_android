package com.taiwanlife.teamwalk.ui.onboarding.model

data class UserInfo(
    val referrerCode: String = "",
    val nickname: String = "",
    val bindingType: String? = null,
    val bindingToken: String? = null,

    // 新的API沒這個欄位 不知道是否後面會需要
    val userAvatar: String? = null,
)