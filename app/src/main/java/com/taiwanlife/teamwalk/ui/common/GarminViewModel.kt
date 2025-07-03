package com.taiwanlife.teamwalk.ui.common

import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.base.UiState
import com.taiwanlife.teamwalk.remote.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.ResponseBody

class GarminViewModel(repository: Repository) : BaseViewModel(repository) {
    val getAuthCodeFlow = RawFlow<ResponseBody>(this)
    val getTokenFlow = RawFlow<ResponseBody>(this)

    fun getGarminAuthCode(authorization: String) {
        getAuthCodeFlow.execute { repository.garmin.getAuthCode(authorization) }
    }

    fun getGarminToken(authorization: String) {
        getTokenFlow.execute { repository.garmin.getToken(authorization) }
    }
}