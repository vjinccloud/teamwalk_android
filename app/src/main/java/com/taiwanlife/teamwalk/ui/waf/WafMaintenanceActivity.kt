package com.taiwanlife.teamwalk.ui.waf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.utils.WafManager
import org.koin.android.ext.android.inject

/**
 * WAF 停機公告頁（0003279）
 *
 * 全螢幕渲染 WAF 回傳的 HTML，使用者停在此頁；
 * 依需求：無重試按鈕、擋返回鍵，要重連只能滑掉 App 重開（重開會再打 checkWaf）。
 */
class WafMaintenanceActivity : AppCompatActivity() {

    private val wafManager: WafManager by inject()

    companion object {
        // HTML/baseUrl 存於 WafManager 單例，設定變更重建時不會遺失（避免自我 finish 逃脫停機頁）
        fun start(context: Context) {
            val intent = Intent(context, WafMaintenanceActivity::class.java).apply {
                // CLEAR_TOP + SINGLE_TOP：已存在就帶回最前、不重複疊
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                // 只有從非 Activity（Application）啟動才需 NEW_TASK；
                // 從 Activity 啟動則留在同一個 task，回前景整個 task 帶前面、維護頁就在最上層
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waf_maintenance)

        val html = wafManager.maintenanceHtml
        val baseUrl = wafManager.maintenanceBaseUrl
        if (html.isNullOrEmpty()) {
            // 沒有內容可顯示，直接關閉不擋
            wafManager.onMaintenanceClosed()
            finish()
            return
        }

        // targetSdk 36 強制 edge-to-edge，將系統列 inset 補成 padding，避免內容畫到狀態列底下
        val root = findViewById<View>(R.id.waf_root)
        WindowCompat.getInsetsController(window, root).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom, left = bars.left, right = bars.right)
            insets
        }

        val webView = findViewById<WebView>(R.id.waf_webview)
        webView.settings.javaScriptEnabled = true
        webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)

        // 擋返回鍵：停機期間不可離開（滑掉 App 重開才會重新檢查）
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 不做任何事，吃掉返回
            }
        })
    }

    override fun onDestroy() {
        // 只有「真正 finish」才解除停機狀態；設定變更(深色模式/字級/分割畫面)重建時不可清除，
        // 否則重建的 onCreate 會讀到空 HTML 自我 finish、放行使用者
        if (isFinishing) {
            wafManager.onMaintenanceClosed()
        }
        super.onDestroy()
    }
}
