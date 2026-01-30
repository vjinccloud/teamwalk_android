package com.taiwanlife.teamwalk.ui.common

import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.garmin.GarminAccessTokenResponse

class GarminViewModel(repository: Repository) : BaseViewModel(repository) {
    val getTokenFlow = RawFlow<GarminAccessTokenResponse>(this)

    fun getGarminToken(redirectUri: String, code: String, verifier: String) {
        getTokenFlow.execute { repository.garmin.getToken2(redirectUri, code, verifier) }
    }
}