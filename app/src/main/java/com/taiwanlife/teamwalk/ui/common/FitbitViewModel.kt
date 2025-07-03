package com.taiwanlife.teamwalk.ui.common

import android.content.Intent
import androidx.core.net.toUri
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.base.UiState
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.fitbit.FitbitGetTokenResponse
import com.taiwanlife.teamwalk.ui.common.model.FitbitData
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.getGson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FitbitViewModel(repository: Repository) : BaseViewModel(repository) {
    val getTokenFlow = RawFlow<FitbitGetTokenResponse>(this)

    fun getUrlIntent(): Intent {
        val url = "https://www.fitbit.com/oauth2/authorize?" +
                "client_id=" + EnvironmentManager.getEnvironmentConfig().connectFitbitClientId + "&" +
                "response_type=code" + "&" +
                "scope=" + "activity%20sleep" + "&" +
                "expires_in=31536000&prompt=login%20consent&redirect_uri=teamwalk" + BuildConfig.BUILD_TYPE + "://webconnect?device=fitbit"
        SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_BIND_FITBIT, getGson().toJson(FitbitData()))
        return Intent(Intent.ACTION_VIEW, url.toUri())
    }

    fun getFitbitToken(
        authorization: String,
        code: String
    ) {
        getTokenFlow.execute {
            repository.fitbit.getToken(
                authorization,
                code,
                repository.fitbit.getGrantType(),
                EnvironmentManager.getEnvironmentConfig().connectGoogleClientId,
                repository.fitbit.getRedirectUrl()
            )
        }
    }
}