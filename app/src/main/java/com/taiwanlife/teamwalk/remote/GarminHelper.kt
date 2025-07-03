package com.taiwanlife.teamwalk.remote

import android.net.Uri
import android.text.TextUtils
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.utils.Utils.randomString
import com.taiwanlife.teamwalk.utils.Utils.sha1
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.Date

object GarminHelper {

    fun getGarminAuthorizationForAuthCode(): String {
        val oauthConsumerKey: String =
            EnvironmentManager.getEnvironmentConfig().connectGarminConsumerKey
        val oauthSignatureMethod = "HMAC-SHA1"

        val oauthNonce = randomString(10)

        val today = Date()
        val timestamp = today.time / 1000L
        val oauthTimestamp = timestamp.toString()
        val oauthVersion = "1.0"

        val signature = "oauth_consumer_key=" + oauthConsumerKey + "&" +
                "oauth_nonce=" + oauthNonce + "&" +
                "oauth_signature_method=" + oauthSignatureMethod + "&" +
                "oauth_timestamp=" + oauthTimestamp + "&" +
                "oauth_version=" + oauthVersion
        var signatureBaseString = ""
        try {
            val signatureBase = URLEncoder.encode(
                Config.GARMIN_BASE_URL + "request_token",
                "utf-8"
            ) + "&" + URLEncoder.encode(signature, "utf-8")
            signatureBaseString = "POST&" + signatureBase
        } catch (e: UnsupportedEncodingException) {
            Timber.d("Fail to encode garmin url")
        }

        val keyString: String =
            EnvironmentManager.getEnvironmentConfig().connectGarminConsumerSecret + "&"
        var oauthSignature: String = ""
        try {
            oauthSignature = URLEncoder.encode(sha1(signatureBaseString, keyString), "utf-8")
        } catch (e: UnsupportedEncodingException) {
            Timber.d("Fail to encode fitbit url")
        } catch (e: NoSuchAlgorithmException) {
            Timber.d("Fail to encode fitbit url")
        } catch (e: InvalidKeyException) {
            Timber.d("Fail to encode fitbit url")
        }

        val authorization = "OAuth " + "oauth_version=\"" + oauthVersion + "\", " +
                "oauth_consumer_key=\"" + oauthConsumerKey + "\", " +
                "oauth_timestamp=\"" + oauthTimestamp + "\", " +
                "oauth_nonce=\"" + oauthNonce + "\", " +
                "oauth_signature_method=\"" + oauthSignatureMethod + "\", " +
                "oauth_signature=\"" + oauthSignature + "\""
        return authorization
    }

    fun parseGetAuthCodeString(
        responseString: String,
        getTsGarminCallback: (String) -> Unit,
        showFailedToast: () -> Unit,
        getUrlCallback: (String) -> Unit
    ) {
        val tsGarmin =
            responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length)
        getTsGarminCallback(tsGarmin)

        val restResponseString = responseString.substring(0, responseString.indexOf("&"))
        if (TextUtils.isEmpty(restResponseString)) {
            showFailedToast
        } else {
//          String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
//                  "oauth_callback=teamwalk" + getString(R.string.env) + "://webconnect?device=garmin@" + ts;
            val url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
                    "oauth_callback=teamwalk" + BuildConfig.BUILD_TYPE + "://webconnectgarmin"

            getUrlCallback(url)
        }
    }

    fun getGarminAuthorizationForAccessToken(
        uri: Uri,
        tsGarmin: String,
        showFailedToast: (reloadWebView: Boolean) -> Unit
    ): String? {
        val oats: String? = tsGarmin

        val oauthConsumerKey: String =
            EnvironmentManager.getEnvironmentConfig().connectGarminConsumerKey
        val oauthToken: String? = uri.getQueryParameter("oauth_token")
        val oauthSignatureMethod = "HMAC-SHA1"

        val oauthNonce: String? = randomString(10)

        val today = Date()
        val timestamp = today.time / 1000L
        val oauthTimestamp = timestamp.toString()
        val oauthVersion = "1.0"
        val oauthVerifier: String? = uri.getQueryParameter("oauth_verifier")

        /**
         * success: teamwalkuat://webconnectgarmin?oauth_token=2c60725c-c48d-4d92-84b5-01a3a334659c&oauth_verifier=nz9Fo2HKpo
         * error: teamwalkuat://webconnectgarmin?oauth_token=63590214-f100-471f-a49f-0a8f1177362e&oauth_verifier=null
         */
        if (oauthVerifier == null || oauthVerifier == "null") {
            showFailedToast(true)
            return null
        }
        val signature = "oauth_consumer_key=" + oauthConsumerKey + "&" +
                "oauth_nonce=" + oauthNonce + "&" +
                "oauth_signature_method=" + oauthSignatureMethod + "&" +
                "oauth_timestamp=" + oauthTimestamp + "&" +
                "oauth_token=" + oauthToken + "&" +
                "oauth_verifier=" + oauthVerifier + "&" +
                "oauth_version=" + oauthVersion
        var signatureBaseString = ""
        try {
            val signatureBase = URLEncoder.encode(
                EnvironmentManager.getEnvironmentConfig().garminUrl + "access_token",
                "utf-8"
            ) + "&" + URLEncoder.encode(signature, "utf-8")
            signatureBaseString = "POST&$signatureBase"
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val keyString = EnvironmentManager.getEnvironmentConfig().connectGarminConsumerSecret + "&" + oats
        var oauthSignature: String? = ""
        try {
            oauthSignature = URLEncoder.encode(sha1(signatureBaseString, keyString), "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }

        val authorization = "OAuth " + "oauth_verifier=\"" + oauthVerifier + "\", " +
                "oauth_version=\"" + oauthVersion + "\", " +
                "oauth_consumer_key=\"" + oauthConsumerKey + "\", " +
                "oauth_token=\"" + oauthToken + "\", " +
                "oauth_timestamp=\"" + oauthTimestamp + "\", " +
                "oauth_nonce=\"" + oauthNonce + "\", " +
                "oauth_signature_method=\"" + oauthSignatureMethod + "\", " +
                "oauth_signature=\"" + oauthSignature + "\""

        return authorization
    }

    fun parseGetTokenString(responseString: String, showFailedToast: () -> Unit): Pair<String, String>? {
        if(TextUtils.isEmpty(responseString)) {
            showFailedToast()
            return null
        }
        val oauthToken = responseString.substring(
            responseString.indexOf("=") + 1,
            responseString.indexOf("&")
        )
        val oauthTokenSecret =
            responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length)

        return Pair<String, String>(oauthToken, oauthTokenSecret)
    }
}