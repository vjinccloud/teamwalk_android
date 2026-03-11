package com.taiwanlife.teamwalk.ui.app_links

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityAppLinksEntryBinding
import com.taiwanlife.teamwalk.databinding.ActivityMainBinding
import com.taiwanlife.teamwalk.ui.connect.ConnectGarminSuccessActivity
import com.taiwanlife.teamwalk.ui.login.LoginSuccessActivity
import com.taiwanlife.teamwalk.ui.main.MainActivity
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast

class AppLinksEntryActivity: BaseActivity<ActivityAppLinksEntryBinding>({ ActivityAppLinksEntryBinding.inflate(it) }) {
    override val statusBarColor: Int = android.R.color.transparent

    override fun onLastCreateBaseActivity(view: View, savedInstanceState: Bundle?) {
        val uri = intent.data

        if (uri != null && isWhitelistedHost(uri)) {
            if(uri.path == "/webconnectgarmin") {
                // 使用 Explicit Intent 開啟內部的 ConnectGarminSuccessActivity
                val nextIntent = Intent(this, ConnectGarminSuccessActivity::class.java).apply {
                    data = uri
                }
                startActivity(nextIntent)
            }
            if(uri.path == "/login" || uri.path == "/loginfailure") {
                // 清除所有資料並開啟登入頁
                Utils.clearLoginData(this, null)
                postEvent(Config.EVENT_TO_LOGIN, "")
            }
            if(uri.path == "/loginsuccess") {
                // 使用 Explicit Intent 開啟內部的 LoginSuccessActivity
                val nextIntent = Intent(this, LoginSuccessActivity::class.java)
                startActivity(nextIntent)
            }
            if(uri.path == "/home") {
                val nextIntent = Intent(this, MainActivity::class.java)
                startActivity(nextIntent)
            }
        }

        finish()
    }
    private fun isWhitelistedHost(uri: Uri): Boolean {
        // 額外多一層檢查，確保 Host 符合預期
        return uri.host == getString(R.string.app_link_host)
    }
}