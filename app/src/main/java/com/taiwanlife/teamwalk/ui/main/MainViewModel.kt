package com.taiwanlife.teamwalk.ui.main

import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse

class MainViewModel(repository: Repository) : BaseViewModel(repository) {
    val userInfoFlow = ApiFlow<UserInfoResponse>(this)

    fun getUserInfo() {
        userInfoFlow.execute {
            repository.api.getUserInfo()
        }
    }
}