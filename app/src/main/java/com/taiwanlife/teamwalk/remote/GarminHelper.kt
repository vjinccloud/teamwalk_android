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

    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        return (1..VERIFIER_LENGTH)
            .map { CHARSET[secureRandom.nextInt(CHARSET.length)] }
            .joinToString("")
    }

    fun generateCodeChallenge(verifier: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(verifier.toByteArray(Charsets.US_ASCII))

        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
    }
}