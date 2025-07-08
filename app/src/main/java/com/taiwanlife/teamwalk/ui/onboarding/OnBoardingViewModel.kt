package com.taiwanlife.teamwalk.ui.onboarding

import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.ui.onboarding.model.UserInfo

class OnBoardingViewModel(repository: Repository) : BaseViewModel(repository) {
    val saveLandingInfoFlow = ApiFlow<Unit>(this)

    fun saveLandingInfo(userInfo: UserInfo) {
        saveLandingInfoFlow.execute {
            repository.api.saveLandingInfo(
                userInfo.referrerCode,
                userInfo.nickname,
                userInfo.bindingApple,
                userInfo.bindingAndroid,
                userInfo.bindingFibit,
                userInfo.bindingFibitToken,
                userInfo.bindingGarminToken
            )
        }
    }
}