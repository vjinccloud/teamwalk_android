package com.taiwanlife.teamwalk.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.ui.onboarding.model.UserInfo
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.getGson
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class OnBoardingActivity<VB : ViewBinding>(private val inflateVB: (LayoutInflater) -> VB) :
    BaseActivity<VB>(inflateVB) {

    companion object {
        const val KEY_USER_INFO = "KEY_USER_INFO"
    }

    protected var userInfo = UserInfo()
    protected val onBoardingViewModel: OnBoardingViewModel by viewModel()

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        intent.getStringExtra(KEY_USER_INFO)?.let {
            userInfo = getGson().fromJson(it, UserInfo::class.java)
        }

        OnBoardingActivityManage.add(this)
        observeOnLifeCycle(onBoardingViewModel.saveLandingInfoFlow.sharedFlow) {
            debugToast(R.string.onboarding_connect_done)

            // 最後關閉所有頁面
            OnBoardingActivityManage.finishAll()
        }
    }

    fun toAvatarActivity() {
        val intent = Intent(this, AvatarActivity::class.java)
        intent.putExtra(KEY_USER_INFO, getGson().toJson(userInfo))
        startActivity(intent)
    }

    fun toConnectActivity() {
        val intent = Intent(this, ConnectActivity::class.java)
        intent.putExtra(KEY_USER_INFO, getGson().toJson(userInfo))
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        OnBoardingActivityManage.remove(this)
    }

    fun saveUserAndFinishAll() {
        onBoardingViewModel.saveLandingInfo(userInfo)
    }
}