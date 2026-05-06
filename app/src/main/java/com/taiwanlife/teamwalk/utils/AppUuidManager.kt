package com.taiwanlife.teamwalk.utils

import com.taiwanlife.teamwalk.MyApplication
import timber.log.Timber
import java.io.File
import java.util.UUID

/**
 * App UUID 管理 — 取代 Firebase Installations ID（FID）作為 appUuid 來源。
 *
 * 行為符合沐創 4 條需求：
 *   1. 升版（同包 install -r）  → 不變（檔案保留）
 *   2. 解除安裝後重裝            → 重產（檔案隨 uninstall 清掉）
 *   3. 換裝置登入同帳號          → 重產（新裝置沒這個檔）
 *   4. 裝置重置 / 備份還原        → 重產（noBackupFilesDir 不被 Auto Backup 還原）
 *
 * 機制：UUID 存在 [android.content.Context.getNoBackupFilesDir]，Android 系統層級
 * 保證該目錄不被 Auto Backup / D2D 轉移帶走。比 backup_rules.xml 排除更可靠
 * （不依賴 OEM ROM 是否正確實作 backup 規則 prefix 比對）。
 *
 * 不做升版遷移：既有 user 升版後 appUuid 會變新值，視為新裝置；user 需重新綁定
 * 健康服務、且原本設定的 pattern login 會失效（fid 是 hash salt）→ 走密碼登入
 * 重新設定即可。此設計由 Kady 拍板（不依賴 Firebase 機制）。
 *
 * 格式：32 字元 hex（無 dash），相容性最佳。
 */
object AppUuidManager {
    private const val FILE_NAME = "app_uuid.txt"

    @Volatile
    private var cached: String? = null

    fun getOrCreate(): String {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val file = File(MyApplication.context.noBackupFilesDir, FILE_NAME)
            val uuid = if (file.exists() && file.length() > 0) {
                file.readText().trim().ifEmpty { generateAndPersist(file) }
            } else {
                generateAndPersist(file)
            }
            cached = uuid
            return uuid
        }
    }

    private fun generateAndPersist(file: File): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        Timber.d("AppUuidManager: generate new UUID")
        file.writeText(uuid)
        return uuid
    }
}
