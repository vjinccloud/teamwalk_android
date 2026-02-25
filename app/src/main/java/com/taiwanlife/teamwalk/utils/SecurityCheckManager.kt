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
    var antiReverseStatus: SecurityStatus = SecurityStatus.INIT

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
            // 每次都檢查
            checkRoot(context)?.let { errors.add(it) }
        }
        if (deviceLockStatus == SecurityStatus.INIT) {
            checkDeviceLock(context)?.let { errors.add(it) }
        }
        if (emulatorStatus == SecurityStatus.INIT) {
            checkEmulator(context)?.let { errors.add(it) }
        }
        if (antiReverseStatus == SecurityStatus.INIT) {
            // 每次都檢查
            checkAntiReverse(context)?.let { errors.add(it) }
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
            rootStatus = SecurityStatus.INIT
//            SecuredPreferenceStoreManager.editAndApply {
//                it.putInt(Config.SP_KNOWS_ROOT, rootStatus.v)
//            }
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_root)
            )
        }
        rootStatus = SecurityStatus.INIT
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

    private fun checkEmulator(context: Context): String? {

        val isEmulator =
            Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.contains("vbox") ||
                    Build.FINGERPRINT.contains("test-keys") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                    "google_sdk" == Build.PRODUCT ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    File("/dev/socket/qemud").exists() ||
                    File("/dev/qemu_pipe").exists()

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

    private fun checkAntiReverse(context: Context): String? {

        val hitCount = listOf(
            detectFridaProcess(),
            detectFridaPorts(),
            detectFridaLibraries(),
            detectFridaThreads(),
            detectSuspiciousProps(),
            detectNativeBridge(),
            detectXposed(),
            detectDebugger(),
            detectPtrace(),
        ).count { it }

        // 命中 >= 2 才視為風險
        if (hitCount >= 2) {
            antiReverseStatus = SecurityStatus.INIT
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_reverse)
            )
        }

        antiReverseStatus = SecurityStatus.INIT
        return null
    }

    private fun detectNativeBridge(): Boolean {
        return try {
            val maps = File("/proc/self/maps")
            if (!maps.exists()) return false

            maps.readText().contains("libhoudini", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    private fun detectSuspiciousProps(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            val output = process.inputStream.bufferedReader().readText()

            output.contains("ro.debuggable=1") ||
                    output.contains("ro.secure=0") ||
                    output.contains("service.adb.root=1")
        } catch (_: Exception) {
            false
        }
    }

    private fun detectFridaThreads(): Boolean {
        return try {
            val taskDir = File("/proc/self/task")
            if (!taskDir.exists()) return false

            taskDir.listFiles()?.any { task ->
                val commFile = File(task, "comm")
                if (commFile.exists()) {
                    val name = commFile.readText()
                    name.contains("frida", ignoreCase = true) ||
                            name.contains("gum", ignoreCase = true)
                } else false
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private fun detectFridaProcess(): Boolean {
        return try {
            val suspiciousProcesses = listOf(
                "frida", "gum-js-loop", "gdbus", "frida-server"
            )
            val process = Runtime.getRuntime().exec("ps")
            val output = process.inputStream.bufferedReader().readText()
            suspiciousProcesses.any { output.contains(it, ignoreCase = true) }
        } catch (_: Exception) {
            false
        }
    }

    private fun detectFridaPorts(): Boolean {
        return try {
            val tcpFile = File("/proc/net/tcp")
            if (!tcpFile.exists()) return false

            val fridaPorts = listOf("69CE", "69D2") // 27042, 27090 的十六進制
            tcpFile.readLines().any { line ->
                fridaPorts.any { port -> line.contains(":$port ", ignoreCase = true) }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun detectFridaLibraries(): Boolean {
        return try {
            val maps = File("/proc/self/maps")
            if (!maps.exists()) return false

            maps.readLines().any {
                it.contains("frida", ignoreCase = true) ||
                        it.contains("gum-js-loop") ||
                        it.contains("libfrida")
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun detectXposed(): Boolean {
        return try {
            // 1. 檢測 XposedBridge 類
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: ClassNotFoundException) {
            // 2. 檢測 Xposed 相關堆疊
            Thread.currentThread().stackTrace.any {
                it.className?.contains("xposed", ignoreCase = true) == true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun detectDebugger(): Boolean {
        return android.os.Debug.isDebuggerConnected() ||
                android.os.Debug.waitingForDebugger()
    }

    private fun detectPtrace(): Boolean {
        return try {
            val status = File("/proc/self/status")
            if (!status.exists()) return false

            status.readLines().any {
                it.startsWith("TracerPid:") &&
                        it.substringAfter(":").trim() != "0"
            }
        } catch (_: Exception) {
            false
        }
    }

}