package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog

class HealthConnectHelper(private val context: Context, activity: AppCompatActivity) {
    private val permissionManager = PermissionManager(activity)

    private val healthConnectPermissions = arrayOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
//        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
//        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class)
    )

    fun availableStatusFlow(): Boolean {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(context, Config.GOOGLE_HEALTH_CONNECT_PACKAGE_NAME)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            context.toast(R.string.main_health_connect_unavailable)
            return false// early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            // Optionally redirect to package installer to find a provider, for example:
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
            return false
        }
        return true
    }

    fun checkPermissions(): Boolean {
        return permissionManager.hasPermissions(context, healthConnectPermissions)
    }

    fun requestPermissionFunction(permissionGratedCallback: () -> Unit) {
        permissionManager.requestPermissions(healthConnectPermissions) { granted, denied ->
            if (granted) {
                permissionGratedCallback()
            } else {
                context.toast(R.string.main_health_connect_permission_denied)
            }
        }
    }

    fun requestPermissionFlow(permissionGratedCallback: () -> Unit) {
        if (!availableStatusFlow()) return

        if (!checkPermissions()) {
            val atLeastOneShowRationale =
                permissionManager.shouldShowRationale(healthConnectPermissions) {}
            if (atLeastOneShowRationale) {
                getAlertDialog(
                    context,
                    context.getString(R.string.main_health_connect_permission_rationale),
                    false,
                    true,
                    context.getString(R.string.confirm1), {
                        requestPermissionFunction(permissionGratedCallback)
                    }
                )
            } else {
                requestPermissionFunction(permissionGratedCallback)
            }
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