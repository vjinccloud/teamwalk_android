package com.taiwanlife.teamwalk.ui.common

import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.fitbit.FitbitGetTokenResponse
import com.taiwanlife.teamwalk.ui.common.model.FitbitData
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.getGson

class FitbitViewModel(repository: Repository) : BaseViewModel(repository) {
    val getTokenFlow = RawFlow<FitbitGetTokenResponse>(this)

    fun getUrl(redirectScheme: String): String {
        val url = "https://www.fitbit.com/oauth2/authorize?" +
                "client_id=" + EnvironmentManager.getEnvironmentConfig().connectFitbitClientId + "&" +
                "response_type=code" + "&" +
                "scope=" + "activity%20sleep" + "&" +
                "expires_in=31536000&prompt=login%20consent&redirect_uri=${redirectScheme}://webconnect?device=fitbit"
        SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_BIND_FITBIT, getGson().toJson(FitbitData()))
        return url
    }

    fun getFitbitToken(
        redirectScheme: String,
        authorization: String,
        code: String
    ) {
        getTokenFlow.execute {
            repository.fitbit.getToken(
                authorization,
                code,
                repository.fitbit.getGrantType(),
                EnvironmentManager.getEnvironmentConfig().connectGoogleClientId,
                repository.fitbit.getRedirectUrl(redirectScheme)
            )
        }
    }
}