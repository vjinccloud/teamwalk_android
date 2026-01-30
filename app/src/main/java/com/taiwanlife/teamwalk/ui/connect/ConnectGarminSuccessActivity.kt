package com.taiwanlife.teamwalk.ui.connect

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityConnectSuccessBinding
import com.taiwanlife.teamwalk.ui.common.GarminViewModel
import com.taiwanlife.teamwalk.ui.common.model.GarminData
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 專門拿來處理Garmin綁定完成之後的流程 避免同時在MainActivity和ConnectActivity要處理DeepLink會出現衝突
 * 這個Activity 使用 TransparentActivityTheme 會是透明的 減少突兀感
 */
class ConnectGarminSuccessActivity : BaseActivity<ActivityConnectSuccessBinding>({
    ActivityConnectSuccessBinding.inflate(it)
}) {
    override val statusBarColor: Int = android.R.color.transparent

    private val garminViewModel: GarminViewModel by viewModel()
    private lateinit var garminData: GarminData

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        handleDeepLink(intent)

        // 觀察呼叫Garmin getToken的結果
        observeOnLifeCycle(garminViewModel.getTokenFlow, onError = {
            toast(R.string.onboard_connect_fail)
            finish()
        }) { garminAccessTokenResponse ->
            if (garminAccessTokenResponse.accessToken.isNotEmpty() && garminAccessTokenResponse.refreshToken.isNotEmpty() && garminAccessTokenResponse.jti.isNotEmpty()) {
                garminData = GarminData(
                    oauthToken = "",
                    oauthTokenSecret = "",
                    accessToken = garminAccessTokenResponse.accessToken,
                    refreshToken = garminAccessTokenResponse.refreshToken,
                    jti = garminAccessTokenResponse.jti
                )

                postEvent(Config.EVENT_GARMIN_CONNECT_DONE, getGson().toJson(garminData))
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
        val uri = intent.data
        if (uri == null) {
            failedEnd()
            return
        }

        val code = uri.getQueryParameter("code")
        val returnedState = uri.getQueryParameter("state")

        val state = SecuredPreferenceStoreManager.getString(Config.SP_GARMIN_STATE, "")
        val verifier = SecuredPreferenceStoreManager.getString(Config.SP_GARMIN_VERIFIER, "")

        if (state.isEmpty() || verifier.isEmpty() || code == null || returnedState == null || state != returnedState) {
            failedEnd()
            return
        }

        getToken(code, verifier)
    }

    private fun getToken(code: String, verifier: String) {
        garminViewModel.getGarminToken(
            "${getString(R.string.redirect_scheme)}://webconnectgarmin",
            code,
            verifier
        )
    }

    private fun failedEnd() {
        toast(R.string.onboard_connect_fail)
        finish()
    }
}