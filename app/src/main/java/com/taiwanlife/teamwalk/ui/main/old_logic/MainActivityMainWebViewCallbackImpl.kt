package com.taiwanlife.teamwalk.ui.main.old_logic

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.remote.GarminHelper
import com.taiwanlife.teamwalk.ui.login.LoginActivity
import com.taiwanlife.teamwalk.ui.main.MainActivity
import com.taiwanlife.teamwalk.ui.main.MainActivity.Companion.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE
import com.taiwanlife.teamwalk.ui.main.MainBottomSheetDialog
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog
import com.taiwanlife.teamwalk.utils.MediaPickerHelper
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.ShareUtil
import com.taiwanlife.teamwalk.utils.WebViewFileWrapper
import timber.log.Timber

/**
 * 第一個版本中先將舊有的Webview和MainActivity解耦
 * 將它們之間的交互拉到interface 並在MainActivity實作
 *
 * 後續Webview網址不同 故行為也已經不同
 * 為了參考以前的邏輯 這個檔案將舊程式碼以Kotlin 重寫後 如果哪天有需要 可以留作參考
 */
class MainActivityMainWebViewCallbackImpl {
//    private val googleSignInLauncherForBind =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, getString(R.string.onboard_connect_fail), Toast.LENGTH_SHORT)
//                    .show()
//                return@registerForActivityResult
//            }
//            googleHealthManager.processGsoLoginResult(
//                result,
//                requestPermissions = { account, fitnessOptions ->
//                    GoogleSignIn.requestPermissions(
//                        this,
//                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
//                        account,
//                        fitnessOptions
//                    )
//                }) { authCode ->
//                googleAuthCode = authCode
//
//                gsoAuthCodeProcessFinish()
//            }
//        }
//    private val googleSignInLauncherForRemove =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            googleHealthManager.disableFit(
//                onFailed = {
//                    Timber.d("Fail to disabling Google Fit")
//                },
//                onSuccess = {
//                    Timber.d("Disabled Google Fit")
//                    // 多跳個Toast 給使用者看
//                    Toast.makeText(
//                        this,
//                        getString(R.string.onboard_connect_remove_success),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                })
//        }
//    override fun showPwErrorDialog() {
//        val alertDialog = getAlertDialog(
//            this,
//            getString(R.string.login_timeout_for_password_change_message),
//            false,
//            false,
//            getString(R.string.confirm),
//            {
//                setPwPageFlag(true)
//                SecuredPreferenceStoreManager.editAndApply { prefEditor ->
//                    prefEditor.putBoolean(Config.PREF_LOGIN_AUTH, false)
//                    prefEditor.putString(Config.PREF_LOGIN_USERNAME, "")
//                    prefEditor.putString(Config.PREF_LOGIN_TICKET, "")
//                    prefEditor.putString(Config.PREF_LOGIN_CASTGC, "")
//                    prefEditor.putString(Config.PREF_LOGIN_TOKEN, "")
//                    prefEditor.putString(Config.PREF_LOGIN_REFRESH_TOKEN, "")
//                    prefEditor.putLong(Config.PREF_LOGIN_EXP, 0L)
//                }
//            })
//        alertDialog.show()
//    }
//
//    override fun enableLoading(enable: Boolean) {
//        viewBinding.loadingIndicator.visibility = if (enable) View.VISIBLE else View.GONE
//    }
//
//    override fun enableLoadingText(enable: Boolean, colorOfText: Int) {
//        viewBinding.loadingText.visibility = if (enable) View.VISIBLE else View.GONE
//        viewBinding.loadingText.setTextColor(colorOfText)
//    }
//
//    override fun showBottomSheetDialog(
//        webViewFileWrapper: WebViewFileWrapper
//    ) {
//        val mediaPickerHelper = MediaPickerHelper(this, "${packageName}.fileprovider") { uri ->
//            if (uri != null) {
//                webViewFileWrapper.onReceiveValue(arrayOf(uri))
//            } else {
//                webViewFileWrapper.onReceiveValue(null)
//            }
//        }
//        val requestPermissions = arrayOf(Manifest.permission.CAMERA)
//        val requestPermissionFunction = {
//            permissionManager.requestPermissions(requestPermissions) { granted, denied ->
//                if (granted) {
//                    mediaPickerHelper.launchCamera()
//                } else {
//                    Toast.makeText(
//                        this@MainActivity,
//                        getString(R.string.camera_permission_denied),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//
//        val mainBottomSheetDialog = MainBottomSheetDialog(this)
//        mainBottomSheetDialog.init(pickImage = {
//            mediaPickerHelper.pickImage()
//        }, takePhotoOnClick = {
//            if (permissionManager.hasPermissions(this, requestPermissions)) {
//                mediaPickerHelper.launchCamera()
//            } else {
//                val atLeastOneRationale =
//                    permissionManager.shouldShowRationale(requestPermissions) {}
//                if (atLeastOneRationale) {
//                    // 提供使用者說明為什麼需要該權限
//                    getAlertDialog(
//                        this,
//                        getString(R.string.camera_permission_rationale),
//                        false,
//                        true,
//                        getString(R.string.confirm), {
//                            requestPermissionFunction()
//                        })
//                } else {
//                    requestPermissionFunction()
//                }
//            }
//        }, pickFile = {
//            mediaPickerHelper.pickFile()
//        }, cancelCallback = {
//            webViewFileWrapper.onReceiveValue(null)
//        })
//        mainBottomSheetDialog.show()
//    }
//
//    override fun getPermissionAndCallback(
//        permissions: java.util.ArrayList<String>,
//        simpleCallback: MainWebViewJava.SimpleCallback?
//    ) {
//        val requestPermissions = permissions.toTypedArray()
//        val requestPermissionFunction = {
//            permissionManager.requestPermissions(requestPermissions) { granted, denied ->
//                if (granted) {
//                    simpleCallback?.callback()
//                } else {
//                    Toast.makeText(
//                        this@MainActivity,
//                        getString(R.string.recognition_permission_denied),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//
//        if (permissionManager.hasPermissions(this, requestPermissions)) {
//            simpleCallback?.callback()
//        } else {
//            val atLeastOneRationale = permissionManager.shouldShowRationale(requestPermissions) {}
//            if (atLeastOneRationale) {
//                // 提供使用者說明為什麼需要該權限
//                getAlertDialog(
//                    this,
//                    getString(R.string.recognition_permission_rationale),
//                    false,
//                    true,
//                    getString(R.string.confirm), {
//                        requestPermissionFunction()
//                    })
//            } else {
//                requestPermissionFunction()
//            }
//        }
//    }
//
//    override fun accessGoogleFit() {
//        val signInIntent = googleHealthManager.loginWithGsoForGoogleFit()
//        googleSignInLauncherForBind.launch(signInIntent)
//    }
//
//    override fun removeGoogleFit() {
//        val signInIntent = googleHealthManager.loginWithGsoForGoogleFit()
//        googleSignInLauncherForRemove.launch(signInIntent)
//    }
//
//    override fun scoreGooglePlay(): Boolean {
//        val manager = ReviewManagerFactory.create(this)
//        val request = manager.requestReviewFlow()
//        request.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                // We got the ReviewInfo object
//                val reviewInfo = task.result
//                val flow = manager.launchReviewFlow(this, reviewInfo)
//                flow.addOnCompleteListener { reviewTask ->
//                    // The flow has finished. The API does not indicate whether the user
//                    // reviewed or not, or even whether the review dialog was shown. Thus, no
//                    // matter the result, we continue our app flow.
//                    if (reviewTask.isSuccessful) {
//                        // 我們沒有權限知道 使用者是否評價完成 或是評價視窗有沒有談出等等 此結果我們無法掌握 故不管成功失敗都結束此流程
//                        Timber.i("scoreGooglePlay: success")
//                    } else {
//                        Timber.i("scoreGooglePlay: fail")
//                    }
//
//                }
//            } else {
//                // There was some problem, log or handle the error code.
//                @ReviewErrorCode
//                val reviewErrorCode = (task.exception as ReviewException).errorCode
//                Timber.d("Review error code: $reviewErrorCode")
//                task.exception?.printStackTrace()
//            }
//        }
//        // 在這裡回傳 true/false 沒有太大意義 Google評分完成是非同步方法 但是舊有程式有回傳值 就先給true
//        return true
//    }
//
//    override fun getStepData(start: Long, end: Long) {
//        TODO("Not yet implemented")
//    }
//
//    override fun toLogin() {
//        val cookieManager = CookieManager.getInstance()
//        cookieManager.removeAllCookies(null)
//        cookieManager.flush()
////        viewBinding.webView.clearCache(true)
////        viewBinding.webView.loadUrl("about:blank")
//
//        val loginIntent = Intent(this, LoginActivity::class.java)
//        loginLauncher.launch(loginIntent)
//    }
//
//    override fun shareContent(shareJsonStr: String): Boolean {
//        return ShareUtil.shareContent(shareJsonStr, this)
//    }
//
//    override fun callGetGarminAuthCode() {
//        val authorization = GarminHelper.getGarminAuthorizationForAuthCode()
//        garminViewModel.getGarminAuthCode(authorization)
////            val responseString = response.string()
////            if (!TextUtils.isEmpty(responseString)) {
////                val (oauthToken, oauthTokenSecret) = GarminHelper.parseGetTokenString(
////                    responseString
////                )
////                Timber.d("Garmin oauthToken - $oauthToken / oauthTokenSecret - $oauthTokenSecret")
////                viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl + "health/connect?device=garmin&t=" + oauthToken + "&s=" + oauthTokenSecret)
////                Toast.makeText(this, getString(R.string.onboard_connect_success), Toast.LENGTH_SHORT)
////                    .show()
////            } else {
////                Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_SHORT).show()
////            }
//    }
//
//
//    private fun setPwPageFlag(pwPageFlag: Boolean) {
//        SecuredPreferenceStoreManager.editAndApply {
//            it.putBoolean(Config.PW_PAGE_FLAG, pwPageFlag)
//        }
//    }
//
//    private fun connectDeviceSuccess(device: String, params: Map<String, String>): String {
//        val urlBuilder =
//            StringBuilder("${EnvironmentManager.getEnvironmentConfig().webUrl}health/connect")
//        // 添加第一個查詢參數 "device"
//        urlBuilder.append("?device=").append(device)
//
//        // 遍歷 params Map，添加其他的查詢參數
//        // 確保每個參數都進行 URL 編碼，以防值中包含特殊字符
//        for ((key, value) in params) {
//            urlBuilder.append("&")
//                .append(key)
//                .append("=")
//                .append(android.net.Uri.encode(value)) // 對值進行 URL 編碼
//        }
//
//        return urlBuilder.toString()
//    }
//
//    //region 參數
//    override fun getPid(): String? {
//        return pid
//    }
//
//    override fun setPid(pid: String?) {
//        this.pid = pid
//    }
//
//    override fun getClearCache(): Boolean? {
//        return clearCache
//    }
//
//    override fun setClearCache(clearCache: Boolean?) {
//        this.clearCache = clearCache
//    }
//
//    override fun getFid(): String? {
//        return fid
//    }
//
//    override fun setFid(fid: String?) {
//        this.fid = fid
//    }
//
//    override fun getTsGarmin(): String? {
//        return tsGarmin
//    }
//
//    override fun setTsGarmin(tsGarmin: String?) {
//        this.tsGarmin = tsGarmin
//    }
//
//    override fun getFCMToken(): String? {
//        return fcmToken
//    }
//
//    override fun setFCMToken(fcmToken: String?) {
//        this.fcmToken = fcmToken
//    }
//
//    override fun getTicket(): String? {
//        return ticket
//    }
//
//    override fun setTicket(ticket: String?) {
//        this.ticket = ticket
//    }
//    //end region
}