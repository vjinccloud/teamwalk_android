package com.taiwanlife.teamwalk.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

    override val statusBarColor: Int = android.R.color.transparent

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        intent.getStringExtra(KEY_USER_INFO)?.let {
            userInfo = getGson().fromJson(it, UserInfo::class.java)
        }

        OnBoardingActivityManage.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        OnBoardingActivityManage.remove(this)
    }

    fun finishAllWithoutSave() {
        OnBoardingActivityManage.finishAll()
    }
}