package com.taiwanlife.teamwalk.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import timber.log.Timber

class ApiLoggingInterceptor : Interceptor {
    companion object{
        const val apiTag = "API_LOG"
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBody = request.body?.let {
            val buffer = Buffer()
            it.writeTo(buffer)
            buffer.readUtf8()
        } ?: "No Body"

        val requestHeadersString = formatHeaders(request.headers)

        Timber.tag(apiTag).d("---API_REQUEST---")
        Timber.tag(apiTag).d("${request.method} ${request.url}")
        Timber.tag(apiTag).v("---Request Headers---")
        Timber.tag(apiTag).v(requestHeadersString)
        Timber.tag(apiTag).v("---Request Body---")
        Timber.tag(apiTag).v(requestBody)
        Timber.tag(apiTag).v("\n")

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Timber.tag(apiTag).e("API_FAILURE: ${request.url} - Error: ${e.message}")
            throw e
        }

        val source = response.body?.source()
        source?.request(Long.MAX_VALUE) // 讀取全部內容
        val responseBodyString = source?.buffer?.clone()?.readUtf8() ?: "No Body"

        Timber.tag(apiTag).d("---API_RESPONSE---")
        Timber.tag(apiTag).d("${response.code} ${request.url}")
        Timber.tag(apiTag).v("---Response Body---")
        Timber.tag(apiTag).v(responseBodyString)
        Timber.tag(apiTag).v("\n")

        return response
    }

    private fun formatHeaders(headers: okhttp3.Headers): String {
        val builder = StringBuilder()
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            // 資安保護：如果是敏感資訊，可以做遮罩
//            val value = if (name.equals("Authorization", ignoreCase = true) ||
//                name.equals("JWT", ignoreCase = true)) {
//                "Bearer ******"
//            } else {
//                headers.value(i)
//            }
            val value = headers.value(i)
            builder.append("   $name: $value\n")
        }
        return if (builder.isEmpty()) "   (empty)" else builder.toString()
    }
}