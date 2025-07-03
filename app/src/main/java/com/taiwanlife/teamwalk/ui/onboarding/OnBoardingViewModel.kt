package com.taiwanlife.teamwalk.ui.onboarding

import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.ui.onboarding.model.UserInfo

class OnBoardingViewModel(repository: Repository) : BaseViewModel(repository) {
    val landingFlow = ApiFlow<Unit>(this)

    fun landing(userInfo: UserInfo) {
        landingFlow.execute {
            repository.api.landing(
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