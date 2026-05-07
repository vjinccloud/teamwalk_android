package com.taiwanlife.teamwalk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import timber.log.Timber

class HealthConnectHelper(private val context: Context, activity: AppCompatActivity) {
//    private val permissionManager = PermissionManager(activity)

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(activity)
    }

    private val healthConnectPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
//        HealthPermission.getWritePermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
//        HealthPermission.getWritePermission(StepsRecord::class),
//        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
//        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class)
    )

    // 明確指定型別，避免 lambda 內部引用 permissionLauncher 造成 recursive type inference 失敗
    private val permissionLauncher: ActivityResultLauncher<Set<String>> = activity.registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(healthConnectPermissions)) {
            permissionGratedCallback?.invoke()
        } else {
            // 0003008: 被拒絕時 dialog + 「前往設定」，跳到 HC 的 per-app 權限頁
            AlertDialog.Builder(activity)
                .setMessage(R.string.main_health_connect_permission_denied)
                .setPositiveButton(R.string.go_to_setting) { _, _ ->
                    activity.openHealthConnectSettings()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private var permissionGratedCallback: (() -> Unit)? = null

    fun availableStatusFlow(): Boolean {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(context, Config.GOOGLE_HEALTH_CONNECT_PACKAGE_NAME)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            context.toast(R.string.main_health_connect_unavailable)
            return false// early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            // Optionally redirect to package installer to find a provider, for example:
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.main_health_connect_not_installed))
                .setPositiveButton(context.getString(R.string.main_health_connect_not_installed_to_store)) { _, _ ->
                    val uriString =
                        "market://details?id=${Config.GOOGLE_HEALTH_CONNECT_PACKAGE_NAME}&url=healthconnect%3A%2F%2Fonboarding"
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setPackage("com.android.vending")
                            data = uriString.toUri()
                            putExtra("overlay", true)
                            putExtra("callerId", context.packageName)
                        }
                    )
                }.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
            return false
        }
        return true
    }

    suspend fun checkPermissions(): Boolean {
        // 包 try-catch 防 HC 服務 binding 失敗（ServiceConnection RemoteException）。
        // 部分裝置 HC 雖回報 SDK_AVAILABLE 但實際 bind service 仍會 RemoteException，
        // 失敗時當作未授權，由 caller 進到請求權限流程。
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            granted.containsAll(healthConnectPermissions)
        } catch (e: Exception) {
            Timber.e(e, "checkPermissions failed (HC service binding?)")
            false
        }
    }

    fun requestPermissionFunction(permissionGratedCallback: () -> Unit) {
        this.permissionGratedCallback = permissionGratedCallback
        permissionLauncher.launch(healthConnectPermissions)
    }

    suspend fun requestPermissionFlow(permissionGratedCallback: () -> Unit) {
        if (!availableStatusFlow()) return

        if (!checkPermissions()) {
            requestPermissionFunction(permissionGratedCallback)
        } else {
            permissionGratedCallback()
        }
    }

    /**
     * 建立一個 Intent，用於開啟 Health Connect 應用程式的權限管理介面。
     * 讓使用者可以手動管理或撤銷已授予的 Health Connect 權限。
     */
    fun createManagePermissionsIntent(context: Context): Intent {
        // Health Connect Client 提供了一個專用的 Intent，用於開啟權限管理頁面
        return HealthConnectClient.getHealthConnectManageDataIntent(
            context,
            Config.GOOGLE_HEALTH_CONNECT_PACKAGE_NAME
        )
    }
}