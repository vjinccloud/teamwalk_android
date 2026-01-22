package com.taiwanlife.teamwalk.ui.connect

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.View
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityConnectSuccessBinding
import com.taiwanlife.teamwalk.ui.common.FitbitViewModel
import com.taiwanlife.teamwalk.ui.common.model.FitbitData
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ConnectFitbitSuccessActivity() : BaseActivity<ActivityConnectSuccessBinding>({
    ActivityConnectSuccessBinding.inflate(it)
}) {
    companion object {
        private const val QUERY_PARAM_TICKET = "ticket"
        private const val QUERY_PARAM_DEVICE = "device"
        private const val QUERY_PARAM_CODE = "code"
        private const val QUERY_PARAM_ERROR = "error"

        private const val DEVICE_FITBIT = "fitbit"
    }

    override val statusBarColor: Int = android.R.color.transparent
    private val fitbitViewModel: FitbitViewModel by viewModel()
    private lateinit var fitbitData: FitbitData

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {

        handleDeepLink(intent)

        // 觀察Fitbit Connect 是否成功
        observeOnLifeCycle(fitbitViewModel.getTokenFlow, onError = {
            toast(R.string.onboard_connect_fail)
            finish()
        }) { fitbitGetTokenResponse ->
            val accessToken = fitbitGetTokenResponse.accessToken
            val refreshToken = fitbitGetTokenResponse.refreshToken
            if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(refreshToken)) {

                fitbitData = fitbitData.copy(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )

                postEvent(Config.EVENT_FITBIT_CONNECT_DONE, getGson().toJson(fitbitData))
                finish()
            } else {
                failedEnd()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val fitbitDataString = SecuredPreferenceStoreManager.getString(Config.SP_BIND_FITBIT, "")
        if (TextUtils.isEmpty(fitbitDataString)) {
            failedEnd()
            return
        }
        fitbitData = getGson().fromJson(fitbitDataString, FitbitData::class.java)

        val uri = intent.data
        if (uri == null) {
            failedEnd()
            return
        }

        Timber.d("Connecting fitbit - ${Utils.formatDate()}")
        val deviceRaw = uri.getQueryParameter(QUERY_PARAM_DEVICE)
        val device = if ((deviceRaw?.indexOf("@") ?: -1) > 0) {
            deviceRaw?.substring(0, deviceRaw.indexOf("@"))
        } else {
            deviceRaw
        }
        if (!device.equals(DEVICE_FITBIT)) {
            failedEnd()
            return
        }

        val code = uri.getQueryParameter(QUERY_PARAM_CODE)
        val error = uri.getQueryParameter(QUERY_PARAM_ERROR)

        if (TextUtils.isEmpty(code) && !TextUtils.isEmpty(error)) {
            Timber.d("Connect fitbit error: $error")
            failedEnd()
        }

        val encodeAuthString =
            "${EnvironmentManager.getEnvironmentConfig().connectFitbitClientId}:${EnvironmentManager.getEnvironmentConfig().connectFitbitClientSecret}"
        val authorizationValue = "Basic ${
            Base64.encodeToString(
                encodeAuthString.toByteArray(),
                Base64.NO_WRAP
            )
        }"
        if (!TextUtils.isEmpty(code)) {
            fitbitViewModel.getFitbitToken(getString(R.string.redirect_scheme), authorizationValue, code!!)
        }
    }

    private fun failedEnd() {
        toast(R.string.onboard_connect_fail)
        finish()
    }
}