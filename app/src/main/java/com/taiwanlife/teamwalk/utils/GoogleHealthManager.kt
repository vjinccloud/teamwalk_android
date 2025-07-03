package com.taiwanlife.teamwalk.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

// 資料類別
sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val displayName: String?,
        val profilePictureUri: String?
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
}

sealed class HealthDataResult {
    data class Success(val data: HealthDataResponse) : HealthDataResult()
    data class Error(val message: String) : HealthDataResult()
}

// 資料模型
data class TokenExchangeRequest(
    val idToken: String
)

data class TokenExchangeResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)

data class HealthDataResponse(
    val bucket: List<DataBucket>
)

data class DataBucket(
    val startTimeMillis: String,
    val endTimeMillis: String,
    val dataset: List<DataSet>
)

data class DataSet(
    val dataSourceId: String,
    val point: List<DataPoint>
)

data class DataPoint(
    val dataTypeName: String,
    val startTimeNanos: String,
    val endTimeNanos: String,
    val value: List<DataValue>
)

data class DataValue(
    val intVal: Int?,
    val fpVal: Double?,
    val stringVal: String?
)


class GoogleHealthManager(private val context: Context) {
    // Google Health API相關的Scopes
    companion object {
        private const val HEALTH_SCOPE = "https://www.googleapis.com/auth/fitness.activity.read"
        private const val PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile"
        private const val EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email"

        private const val FITNESS_ACTIVITY_SCOPE =
            "https://www.googleapis.com/auth/fitness.activity.read"
        private const val FITNESS_SLEEP_SCOPE = "https://www.googleapis.com/auth/fitness.sleep.read"
    }

    private val credentialManager = CredentialManager.create(context)
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()

    /**
     * 之後會被取代的方法 但現階段先接著用
     */
    fun loginWithGsoForGoogleFit(): Intent {
        val serverClientId: String = EnvironmentManager.getEnvironmentConfig().connectGoogleClientId

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(
                Scope(FITNESS_ACTIVITY_SCOPE),
                Scope(FITNESS_SLEEP_SCOPE)
            )
            .requestServerAuthCode(serverClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            googleSignInClient.signOut()
        }
        return googleSignInClient.signInIntent
    }

    fun processGsoLoginResult(
        result: ActivityResult,
        requestPermissions: (account: GoogleSignInAccount, fitnessOptions: FitnessOptions) -> Unit,
        getAuthCode: (String?) -> Unit
    ) {
        val data = result.data

        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            requestPermissions(account, fitnessOptions)
        } else {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var authCode: String? = null
            try {
                val googleAccount = task.getResult<ApiException?>(ApiException::class.java)
                authCode = googleAccount.serverAuthCode
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(context, R.string.onboard_connect_fail, Toast.LENGTH_SHORT).show()
            }
            getAuthCode(authCode)
        }
    }

    fun disableFit(onSuccess: () -> Unit, onFailed: () -> Unit) {
        Fitness.getConfigClient(
            context,
            GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        )
            .disableFit()
            .addOnCompleteListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailed()
            }
    }

    // 使用新的Google Identity Services進行登入
    suspend fun signInWithGoogle(): GoogleSignInResult {
        return withContext(Dispatchers.IO) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(EnvironmentManager.getEnvironmentConfig().connectGoogleClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                handleSignInResult(result)

            } catch (e: GetCredentialException) {
                GoogleSignInResult.Error("登入失敗: ${e.message}")
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): GoogleSignInResult {
        return when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        GoogleSignInResult.Success(
                            idToken = googleIdTokenCredential.idToken,
                            displayName = googleIdTokenCredential.displayName,
                            profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString()
                        )

                    } catch (e: Exception) {
                        GoogleSignInResult.Error("處理登入結果失敗: ${e.message}")
                    }
                } else {
                    GoogleSignInResult.Error("未知的憑證類型")
                }
            }

            else -> GoogleSignInResult.Error("不支援的憑證")
        }
    }

//    // 獲取OAuth Access Token用於Health API
//    suspend fun getAccessTokenForHealthAPI(idToken: String): String? {
//        return withContext(Dispatchers.IO) {
//            try {
//                // 這裡需要將ID Token交換為Access Token
//                // 通常需要透過你的後端服務器來完成
//                exchangeIdTokenForAccessToken(idToken)
//            } catch (e: Exception) {
//                null
//            }
//        }
//    }

//    private suspend fun exchangeIdTokenForAccessToken(idToken: String): String? {
//        // 實際實作中，你需要：
//        // 1. 將idToken發送到你的後端服務器
//        // 2. 後端驗證idToken並使用OAuth2流程獲取access token
//        // 3. 返回access token給客戶端
//
//        // 這是一個示例實作，實際使用時請替換為你的後端API
//        return try {
//            val tokenService = createTokenService()
//            val response = tokenService.exchangeToken(idToken)
//            response.accessToken
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    // 獲取Google Health數據
//    suspend fun getHealthData(accessToken: String): HealthDataResult {
//        return withContext(Dispatchers.IO) {
//            try {
//                val healthService = createHealthService()
//                val response = healthService.getActivityData("Bearer $accessToken")
//
//                HealthDataResult.Success(response)
//            } catch (e: Exception) {
//                HealthDataResult.Error("獲取健康數據失敗: ${e.message}")
//            }
//        }
//    }
//
//    private fun createTokenService(): TokenExchangeService {
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://your-backend-api.com/")
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(TokenExchangeService::class.java)
//    }
//
//    private fun createHealthService(): GoogleHealthService {
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://www.googleapis.com/fitness/v1/")
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(GoogleHealthService::class.java)
//    }
}