package com.taiwanlife.teamwalk.remote

import android.text.TextUtils
import android.util.Base64
import androidx.core.net.toUri
import com.taiwanlife.teamwalk.EnvironmentManager
import java.security.MessageDigest
import java.security.SecureRandom

object GarminHelper {
    private const val VERIFIER_LENGTH = 64
    private const val CHARSET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"

    /**
     * Garmin驗證用網址
     */
    fun buildGarminAuthUrl(
        redirectUri: String,
//        scope: String,
        state: String,
        codeChallenge: String
    ): String {
        return EnvironmentManager.getEnvironmentConfig().connectGarminPortal.toUri()
            .buildUpon()
            .appendQueryParameter(
                "client_id",
                EnvironmentManager.getEnvironmentConfig().connectGarminConsumerKey
            )
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirectUri)
//            .appendQueryParameter("scope", scope)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
            .toString()
    }

    /**
     * 建立隨機驗證碼
     */
    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        return (1..VERIFIER_LENGTH)
            .map { CHARSET[secureRandom.nextInt(CHARSET.length)] }
            .joinToString("")
    }

    /**
     * 加密隨機碼
     */
    fun generateCodeChallenge(verifier: String): String {
        // Cryptographic hash finalized without update 避免資安誤判沒有呼叫update 直接呼叫update => digest
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(verifier.toByteArray(Charsets.US_ASCII))
        val bytes = messageDigest.digest()

        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
    }
}