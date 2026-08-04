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

    private var isSubmitting = false
    private var lockedButton: View? = null

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        intent.getStringExtra(KEY_USER_INFO)?.let {
            userInfo = getGson().fromJson(it, UserInfo::class.java)
        }

        OnBoardingActivityManage.add(this)
    }

    /**
     * 送出 landing 資料。送出期間把「下一步」鎖起來。
     *
     * 這幾頁的跳頁不是點下去就跳，是「打 API → flow 回來 → startActivity」，
     * 所以連點兩下會送出兩次 API、疊出兩個 Activity（使用者會看到跳兩頁、要返回兩次）。
     *
     * 解鎖時機有兩個，缺一不可：
     *   - onResume()：從下一頁返回時
     *   - releaseSubmitLock()：API 失敗時，由各頁的 observeOnLifeCycle(onError = ...) 呼叫
     * 少了後者，API 一失敗按鈕就永久按不動，比連點更嚴重。
     */
    protected fun saveLandingInfoOnce(nextButton: View) {
        if (isSubmitting) return
        isSubmitting = true
        lockedButton = nextButton
        nextButton.isEnabled = false
        onBoardingViewModel.saveLandingInfo(userInfo)
    }

    protected fun releaseSubmitLock() {
        isSubmitting = false
        lockedButton?.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        releaseSubmitLock()
    }

    override fun onDestroy() {
        super.onDestroy()
        OnBoardingActivityManage.remove(this)
    }

    fun finishAllWithoutSave() {
        OnBoardingActivityManage.finishAll()
    }
}