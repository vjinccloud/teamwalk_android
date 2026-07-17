package com.taiwanlife.teamwalk.utils

import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.MyApplication
import com.taiwanlife.teamwalk.remote.ApiRepository
import com.taiwanlife.teamwalk.ui.waf.WafMaintenanceActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * WAF 停機檢查（0003279）
 *
 * 開 App 與回前景時呼叫 checkWaf：
 *  - 回 JSON  → WAF 正常，放行（不做事）
 *  - 回非 JSON(HTML) → WAF 停機公告，全螢幕渲染該 HTML，使用者停在該頁（滑掉重開才會再檢查）
 *  - 連不到/錯誤 → 依需求「不擋」，交由原本流程處理
 */
class WafManager(private val apiRepository: ApiRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // 是否正顯示停機頁（避免回前景時重複開）
    @Volatile
    var isMaintenanceShowing = false
        private set

    // 停機頁 HTML 與 baseUrl 存在此（單例，跨 Activity 設定變更重建仍在；App 被殺→新 process→自動清空）
    @Volatile
    var maintenanceHtml: String? = null
        private set

    @Volatile
    var maintenanceBaseUrl: String? = null
        private set

    // 是否正在檢查中（避免同時多次觸發）
    @Volatile
    private var checking = false

    fun check() {
        // 已在停機模式：回前景時把維護頁重新帶回最前（避免主 task 的登入/首頁蓋掉它），不重打 API
        if (isMaintenanceShowing) {
            launchMaintenanceActivity()
            return
        }
        if (checking) return
        checking = true
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { apiRepository.checkWaf() }
                // 非 2xx 時 body 為 null，停機頁可能帶非 200 狀態，需一併看 errorBody
                val rawBody = response.body() ?: response.errorBody()
                val subtype = rawBody?.contentType()?.subtype?.lowercase().orEmpty()
                val body = withContext(Dispatchers.IO) { rawBody?.string() }.orEmpty()
                val trimmed = body.trimStart()

                // 以 Content-Type 為主，body 開頭字元為輔，雙重判斷避免 header 缺失時誤判
                val isJson = subtype.contains("json") ||
                    trimmed.startsWith("{") || trimmed.startsWith("[")
                val looksHtml = subtype.contains("html") || trimmed.startsWith("<")

                when {
                    isJson -> {
                        // WAF 正常，走原本流程
                        Timber.d("checkWaf: JSON 正常放行")
                    }
                    looksHtml && body.isNotBlank() -> {
                        // 非 JSON 且像 HTML → 視為停機公告，渲染於畫面
                        showMaintenance(body)
                    }
                    else -> {
                        // 既非 JSON 也不像 HTML(空/未知)：不擋，交由原本流程
                        Timber.w("checkWaf: 回傳非 JSON 亦非 HTML，略過不擋")
                    }
                }
            } catch (e: Exception) {
                // 連不到 / timeout / 例外：不擋，交由原本流程
                Timber.w(e, "checkWaf 失敗，忽略不擋")
            } finally {
                checking = false
            }
        }
    }

    private fun showMaintenance(html: String) {
        isMaintenanceShowing = true
        maintenanceHtml = html
        // 用 api host 當 baseUrl，讓停機頁的相對資源（css/圖）能解析
        maintenanceBaseUrl = EnvironmentManager.getEnvironmentConfig().apiUrl
        launchMaintenanceActivity()
    }

    /**
     * 盡量開在「當前畫面的同一個 task」上面，這樣回前景時整個 task 被帶到前面、維護頁就在最上層；
     * 沒有前景 Activity 時（極少）才退回用 Application context + NEW_TASK。
     */
    private fun launchMaintenanceActivity() {
        val activity = MyApplication.currentActivity
        WafMaintenanceActivity.start(activity ?: MyApplication.context)
    }

    /** 停機頁真正關閉時回呼（設定變更重建時不呼叫；正常只有 App 被殺才會走到） */
    fun onMaintenanceClosed() {
        isMaintenanceShowing = false
        maintenanceHtml = null
        maintenanceBaseUrl = null
    }
}
