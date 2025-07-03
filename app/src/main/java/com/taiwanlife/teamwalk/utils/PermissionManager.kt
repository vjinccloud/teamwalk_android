package com.taiwanlife.teamwalk.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.taiwanlife.teamwalk.R

class PermissionManager(
    private val activity: ComponentActivity
) {

    private var permissionCallback: ((granted: Boolean, deniedPermissions: List<String>) -> Unit)? =
        null

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val denied = result.filterValues { !it }.keys.toList()
        permissionCallback?.invoke(denied.isEmpty(), denied)
        permissionCallback = null
    }

    /**
     * 檢查是否所有權限都已授予
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 請求權限（會自動處理 rationale，並透過 callback 回傳結果）
     */
    fun requestPermissions(
        permissions: Array<String>,
        onResult: (granted: Boolean, deniedPermissions: List<String>) -> Unit
    ) {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            onResult(true, emptyList())
        } else {
            permissionCallback = onResult
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    /**
     * 是否應該顯示權限說明（rationale）
     */
    fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 是否應該顯示權限說明（rationale）
     */
    fun shouldShowRationale(
        permissions: Array<String>,
        shouldShowRationaleOn: (permission: String) -> Unit
    ): Boolean {
        var atLeastOneRationale = false
        permissions.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                atLeastOneRationale = true
                shouldShowRationaleOn(it)
            }
        }
        return atLeastOneRationale
    }

    fun getPermissionRationaleText(permission: String, context: Context): String? {
        return when (permission) {
            Manifest.permission.ACTIVITY_RECOGNITION -> context.getString(R.string.recognition_permission_rationale)
            Manifest.permission.CAMERA -> context.getString(R.string.camera_permission_rationale)
            Manifest.permission.POST_NOTIFICATIONS -> context.getString(R.string.notification_permission_rationale)
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> context.getString(R.string.write_permission_rationale)
            Manifest.permission.READ_EXTERNAL_STORAGE -> context.getString(R.string.read_permission_rationale)

            else -> null
        }
    }

    fun getPermissionDeniedText(permission: String, context: Context): String? {
        return when (permission) {
            Manifest.permission.ACTIVITY_RECOGNITION -> context.getString(R.string.recognition_permission_denied)
            Manifest.permission.CAMERA -> context.getString(R.string.camera_permission_denied)
            Manifest.permission.POST_NOTIFICATIONS -> context.getString(R.string.notification_permission_denied)
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> context.getString(R.string.write_permission_denied)
            Manifest.permission.READ_EXTERNAL_STORAGE -> context.getString(R.string.read_permission_denied)

            else -> null
        }
    }
}
