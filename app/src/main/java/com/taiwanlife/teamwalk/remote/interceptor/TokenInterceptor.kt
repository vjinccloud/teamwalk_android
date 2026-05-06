package com.taiwanlife.teamwalk.remote.interceptor

import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {

    companion object {
        // OkHttp 客製 UA：加上 App 標記與版號，後端可依此辨識真實 App vs 外部偽造
        // 範例：okhttp/5.3.2 taiwanlife_teamwalk_app/3.0.41
        private val CUSTOM_USER_AGENT_SUFFIX =
            " taiwanlife_teamwalk_app/${BuildConfig.VERSION_NAME}"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toUrl().toString()
        val encodedUrl = originalRequest.url.encodedPath

        // 所有 request 都帶上 App 識別資訊（UA 後綴 + 獨立 header）
        // 後端可以選擇 parse UA 或直接讀 X-App-* header，兩種任選
        val baseUserAgent = originalRequest.header("User-Agent") ?: "okhttp"
        val builder = originalRequest.newBuilder()
            .header("User-Agent", baseUserAgent + CUSTOM_USER_AGENT_SUFFIX)
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-App-Version-Code", BuildConfig.VERSION_CODE.toString())
            .header("X-App-Platform", "Android")
            .header("X-App-Identifier", BuildConfig.APPLICATION_ID)

        // 判斷是否該加 Authorization (JWT)
        val isLoginPath = encodedUrl.endsWith(Config.API_LOGIN_PATH)
        val isApiUrl = url.contains(EnvironmentManager.getEnvironmentConfig().apiUrl)
        val shouldAddJwt = isApiUrl && !isLoginPath

        if (shouldAddJwt) {
            val jwtToken = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT, "")
            if (jwtToken.isNotEmpty()) {
                builder.header("Authorization", jwtToken)
                // 註：原作者註解說這邊本來該加 "Bearer " 前綴但被改掉了
                // .header("Authorization", "Bearer $jwtToken")
            }
        }

        return chain.proceed(builder.build())
    }
}
