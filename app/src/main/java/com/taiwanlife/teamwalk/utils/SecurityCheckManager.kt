package com.taiwanlife.teamwalk.utils

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.taiwanlife.teamwalk.BuildConfig
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

    fun runSecurityCheck(context: Context): SecurityCheckResult {
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
        // Root較嚴重 有的話就需要關閉
//        if (rootStatus == SecurityStatus.INIT) {
//            checkRoot(context)?.let { errors.add(it) }
//        }
        if (deviceLockStatus == SecurityStatus.INIT) {
            checkDeviceLock(context)?.let { errors.add(it) }
        }
        // 模擬器較嚴重 有的話就需要關閉
//        if (emulatorStatus == SecurityStatus.INIT) {
//            checkEmulator(context)?.let { errors.add(it) }
//        }
        // 逆向工具較嚴重 有的話就需要關閉 以移至runShutdownCheck檢查
//        if (antiReverseStatus == SecurityStatus.INIT) {
//            // 每次都檢查
//            checkAntiReverse(context)?.let { errors.add(it) }
//        }

        return if (errors.isNotEmpty()) {
            SecurityCheckResult(
                false,
                errorMessage = errors.joinToString(separator = "\n\n")
            )
        } else {
            SecurityCheckResult(true)
        }
    }

    fun runShutdownCheck(context: Context): SecurityCheckResult {
        val errors = mutableListOf<String>()
        if (emulatorStatus == SecurityStatus.INIT) {
            // 每次都會執行
            checkEmulator(context)?.let { errors.add(it) }
        }
        if (rootStatus == SecurityStatus.INIT) {
            // 每次都會執行
            checkRoot(context)?.let { errors.add(it) }
        }
        if (antiReverseStatus == SecurityStatus.INIT) {
            // 每次都檢查
            checkAntiReverse(context)?.let { errors.add(it) }
        }

        return if (errors.isNotEmpty()) {
            SecurityCheckResult(
                false,
                errorMessage = errors.joinToString(separator = "\n\n")
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

        val highRisk = listOf(
            hasSuBinary(),                 // 有 su 幾乎可確定 root
            isSystemWritable(),            // system 可寫
            detectSuspiciousProps()        // ro.debuggable=1 等
        ).any { it }

        val mediumRiskCount = listOf(
            hasTestKeys(),                 // test-keys
            !isSelinuxEnforced(),          // SELinux 非 Enforcing 才算命中
            hasRootManagementApp(context)  // Magisk / KernelSU / LSPosed
        ).count { it }

        val emulatorRoot = detectNoxRoot() && hasSuBinary()

        if (highRisk || mediumRiskCount >= 2 || emulatorRoot) {
            rootStatus = SecurityStatus.INIT
//            SecuredPreferenceStoreManager.editAndApply {
//                it.putInt(Config.SP_KNOWS_ROOT, SecurityStatus.NOTIFIED.v)
//            }
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_check),
                context.getString(R.string.main_security_check_root)
            )
        }

        rootStatus = SecurityStatus.PASSED
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
                    Build.FINGERPRINT.contains("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                    "google_sdk" == Build.PRODUCT ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    File("/dev/socket/qemud").exists()

        val fileIsEmulator = listOf(
            "/dev/socket/qemud",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd",
            "/dev/qemu_pipe",
            "/system/qemu_trace",
            "/system/bin/qemu_props",
            "/system/bin/nox-prop",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/data/property/persist.nox.render",
            "/sys/module/vboxguest"
        ).any { File(it).exists() }

        if (isEmulator || fileIsEmulator) {
            emulatorStatus = SecurityStatus.INIT
            return String.format(
                Locale.getDefault(),
                context.getString(R.string.main_security_force_close),
                context.getString(R.string.main_security_check_emulator)
            )
        }

        emulatorStatus = SecurityStatus.INIT
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
            "/system/bin/.ext/.su",
            "/bin/su",
            "/xbin/su",
            "/system/app/Superuser.apk"
        )

        if (paths.any { File(it).exists() }) return true

        return try {
            Runtime.getRuntime()
                .exec(arrayOf("which", "su"))
                .inputStream
                .bufferedReader()
                .readLine() != null
        } catch (_: Exception) {
            false
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

    private fun detectNoxRoot(): Boolean {
        return Build.HARDWARE.contains("nox", ignoreCase = true) ||
                Build.BOARD.contains("nox", ignoreCase = true) ||
                Build.BOOTLOADER.contains("nox", ignoreCase = true) ||
                File("/system/bin/nox-prop").exists() ||
                File("/system/bin/nox-vbox-guest").exists()
    }

    private fun checkAntiReverse(context: Context): String? {
        val highRisk = listOf(
            detectPtrace(),    // Native 劫持
            detectXposed() // Xposed 注入
        ).any { it }

        val mediumRiskCount = listOf(
            detectDebugger(),
            detectFridaProcess(),
            detectFridaPorts(),
            detectFridaLibraries(),
            detectFridaThreads(),
            detectSuspiciousProps(), // ro.debuggable=1 等
            detectNativeBridge() // 模擬器轉譯層
        ).count { it }

        if (highRisk || mediumRiskCount >= 2) {
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
            // 讀取 /proc/net/tcp 檢查所有正在 LISTEN 的本地端口
            val tcpFile = File("/proc/net/tcp")
            if (!tcpFile.exists()) return false

            // 不只檢查 69CE(27042)，只要有異常的本地監聽都該警覺
            // 但為了避免誤傷，這裡維持檢查清單，並建議加入 27047 (常用的跳轉 Port)
            val fridaPorts = listOf("69CE", "69D2", "69A7")
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

            maps.readLines().any { line ->
                // 檢查關鍵字
                val hasKeyword = line.contains("frida", ignoreCase = true) ||
                        line.contains("gum-js", ignoreCase = true)

                // 進階：檢查是否存在刪除後的檔案映射 (Frida 注入後常會刪除 temp 檔)
                val isDeleted =
                    line.contains("(deleted)") && (line.contains(".so") || line.contains("tmp"))

                hasKeyword || isDeleted
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun detectXposed(): Boolean {
        return try {
            // 檢查類別
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: Exception) {
            // 檢查環境變數
            val classpath = System.getProperty("java.class.path")
            if (classpath?.contains("XposedBridge") == true) return true

            // 檢查堆疊
            Thread.currentThread().stackTrace.any {
                it.className?.contains("xposed", ignoreCase = true) == true
            }
        }
    }

    private fun detectDebugger(): Boolean {
        if (BuildConfig.DEBUG) {
            // Debug版本打開為正常
            return false
        }

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