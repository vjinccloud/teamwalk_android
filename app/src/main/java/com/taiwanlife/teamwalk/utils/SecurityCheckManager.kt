package com.taiwanlife.teamwalk.utils

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import java.io.File
import java.util.Locale

data class SecurityCheckResult(
    val passed: Boolean,
    val errorMessage: String? = null
)

enum class SecurityStatus(val v: Int) {
    INIT(0), PASSED(1), NOTIFIED(2);

    companion object {
        fun fromValue(value: Int): SecurityStatus {
            return entries.find { it.v == value } ?: INIT
        }
    }
}

object SecurityCheckManager {
    var overlayStatus: SecurityStatus = SecurityStatus.INIT
    var developerModeStatus: SecurityStatus = SecurityStatus.INIT
    var usbDebugStatus: SecurityStatus = SecurityStatus.INIT
    var rootStatus: SecurityStatus = SecurityStatus.INIT
    var deviceLockStatus: SecurityStatus = SecurityStatus.INIT
    var emulatorStatus: SecurityStatus = SecurityStatus.INIT

    fun runAll(context: Context): SecurityCheckResult {
        val errors = mutableListOf<String>()
//        val root = SecuredPreferenceStoreManager.getInt(Config.SP_KNOWS_ROOT, SecurityStatus.INIT.v)
//        rootStatus = SecurityStatus.fromValue(root)

        if (overlayStatus == SecurityStatus.INIT) {
            checkOverlay(context)?.let { errors.add(it) }
        }
        if (developerModeStatus == SecurityStatus.INIT) {
            checkDeveloperMode(context)?.let { errors.add(it) }
        }
        if (usbDebugStatus == SecurityStatus.INIT) {
            checkUsbDebug(context)?.let { errors.add(it) }
        }
        if(rootStatus == SecurityStatus.INIT) {
            checkRoot(context)?.let { errors.add(it) }
        }
        if (deviceLockStatus == SecurityStatus.INIT) {
            checkDeviceLock(context)?.let { errors.add(it) }
        }
        if (emulatorStatus == SecurityStatus.INIT) {
            checkEmulator(context)?.let { errors.add(it) }
        }
//        checkAppDebuggable(context)?.let { return it }
//        checkInstallSource(context)?.let { return it }

        return if (errors.isNotEmpty()) {
            SecurityCheckResult(
                false,
                errors.joinToString(separator = "\n\n")
            )
        } else {
            SecurityCheckResult(true)
        }
    }

    private fun checkOverlay(context: Context): String? {
        if (Settings.canDrawOverlays(context)) {
            overlayStatus = SecurityStatus.NOTIFIED
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_overlay)
            )
        }
        overlayStatus = SecurityStatus.PASSED
        return null
    }


    private fun checkDeveloperMode(context: Context): String? {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1

        if (enabled) {
            developerModeStatus = SecurityStatus.NOTIFIED
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_developer)
            )
        }
        developerModeStatus = SecurityStatus.PASSED
        return null
    }


    private fun checkUsbDebug(context: Context): String? {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) == 1

        if (enabled) {
            usbDebugStatus = SecurityStatus.NOTIFIED
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_usb)
            )
        }
        usbDebugStatus = SecurityStatus.PASSED
        return null
    }

    fun checkRoot(context: Context): String? {

        val hitCount = listOf(
            hasTestKeys(),
            isSelinuxEnforced(),
            hasSuBinary(),
            hasRootManagementApp(context),
            isSystemWritable(),
        ).count { it }

        if (hitCount >= 2) {
            rootStatus = SecurityStatus.NOTIFIED
//            SecuredPreferenceStoreManager.editAndApply {
//                it.putInt(Config.SP_KNOWS_ROOT, rootStatus.v)
//            }
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_root)
            )
        }
        rootStatus = SecurityStatus.PASSED
//        SecuredPreferenceStoreManager.editAndApply {
//            it.putInt(Config.SP_KNOWS_ROOT, rootStatus.v)
//        }
        return null
    }

    private fun checkDeviceLock(context: Context): String? {

        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isDeviceSecure) {
            deviceLockStatus = SecurityStatus.NOTIFIED
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_no_secure)
            )
        }
        deviceLockStatus = SecurityStatus.PASSED
        return null
    }

//    private fun checkAppDebuggable(context: Context): SecurityCheckResult? {
//        val debuggable =
//            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
//
//        if (debuggable) {
//            return SecurityCheckResult(
//                false,
//                "應用程式目前為除錯模式，基於安全性無法執行"
//            )
//        }
//        return null
//    }

//    private fun checkInstallSource(context: Context): SecurityCheckResult? {
//        val installer = context.packageManager
//            .getInstallSourceInfo(context.packageName)
//            .installingPackageName
//
//        val allowed = setOf(
//            "com.android.vending",              // Play Store
//            "com.google.android.packageinstaller"
//        )
//
//        if (installer !in allowed) {
//            return SecurityCheckResult(
//                false,
//                "應用程式來源不明，請透過官方管道安裝"
//            )
//        }
//        return null
//    }

    private fun checkEmulator(context: Context): String? {
        val isEmulator =
            Build.FINGERPRINT.contains("generic") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.BRAND.startsWith("generic") ||
                    Build.DEVICE.startsWith("generic") ||
                    Build.PRODUCT.contains("sdk")

        if (isEmulator) {
            emulatorStatus = SecurityStatus.NOTIFIED
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_emulator)
            )
        }
        emulatorStatus = SecurityStatus.PASSED
        return null
    }


    private fun hasTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    private fun isSelinuxEnforced(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            val output = process.inputStream.bufferedReader().readLine()
            output.equals("Enforcing", ignoreCase = true)
        } catch (_: Exception) {
            true // 無法判斷時，採保守「不命中」
        }
    }

    private fun hasSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/system/bin/.ext/.su"
        )

        return paths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun hasRootManagementApp(context: Context): Boolean {
        val pkgs = listOf(
            "com.topjohnwu.magisk",
            "io.github.vvb2060.magisk",
            "me.weishu.kernelsu",
            "org.lsposed.manager"
        )

        val pm = context.packageManager
        return pkgs.any {
            try {
                pm.getPackageInfo(it, 0)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun isSystemWritable(): Boolean {
        return try {
            val process = Runtime.getRuntime()
                .exec(arrayOf("sh", "-c", "mount | grep ' /system '"))
            process.inputStream.bufferedReader()
                .readText()
                .contains("rw")
        } catch (_: Exception) {
            false
        }
    }
}