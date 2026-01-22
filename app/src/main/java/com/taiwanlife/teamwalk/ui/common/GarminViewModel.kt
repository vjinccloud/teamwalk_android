package com.taiwanlife.teamwalk.ui.common

import android.util.Base64
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.garmin.GarminAccessTokenResponse
import okhttp3.ResponseBody

class GarminViewModel(repository: Repository) : BaseViewModel(repository) {
    val getAuthCodeFlow = RawFlow<ResponseBody>(this)
    val getTokenFlow = RawFlow<ResponseBody>(this)
    val getTokenFlow2 = RawFlow<GarminAccessTokenResponse>(this)

    fun getGarminAuthCode(authorization: String) {
        getAuthCodeFlow.execute { repository.garmin.getAuthCode(authorization) }
    }

    fun getGarminToken2(redirectUri: String, code: String, verifier: String) {
        getTokenFlow2.execute { repository.garmin.getToken2(redirectUri, code, verifier) }
    }

    fun getGarminToken(authorization: String) {
        getTokenFlow.execute { repository.garmin.getToken(authorization) }
    }

    private fun createBasicAuthHeader(clientId: String): String {
        val raw = "$clientId:"
        val encoded = Base64.encodeToString(
            raw.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        return "Basic $encoded"
    }
}