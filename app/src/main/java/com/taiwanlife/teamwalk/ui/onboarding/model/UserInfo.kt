package com.taiwanlife.teamwalk.ui.onboarding.model

data class UserInfo(
    val referrerCode: String = "",
    val nickname: String = "",
    val bindingApple: Boolean = false,
    val bindingAndroid: Boolean = false,
    val bindingFibit: Boolean = false,
    val bindingFibitToken: String = "",
    val bindingGarminToken: String = "",


    // 新的API沒這個欄位 不知道是否後面會需要
    val bindingGarmin: Boolean = false,

    // 新的API沒這個欄位 不知道是否後面會需要
    val userAvatar: String? = null,
)