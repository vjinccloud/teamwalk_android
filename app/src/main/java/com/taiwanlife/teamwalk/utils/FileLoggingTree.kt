package com.taiwanlife.teamwalk.utils
import android.content.Context
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileLoggingTree(private val context: Context) : Timber.DebugTree() {

    companion object {
        const val LOG_FILE_NAME = "app_internal_logs.txt"

        /**
         * 刪除 Log 檔案的邏輯
         */
        fun clearLogs(context: Context) {
            try {
                val file = File(context.filesDir, LOG_FILE_NAME)
                if (file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        // 這裡不能用 Timber，因為此時 Tree 可能還沒掛載
                        Timber.tag("FileLoggingTree").d("舊的 Log 檔案已成功刪除")
                    }
                }
            } catch (e: Exception) {
                Timber.tag("FileLoggingTree").e("刪除 Log 檔案失敗: ${e.message}")
            }
        }
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        // 2. 過濾機制：只有 Tag 是 "API_LOG" 的才寫入檔案
        if (tag != "API_LOG") {
            return
        }

        val timestamp = dateFormatter.format(Date())
        val priorityStr = when (priority) {
            2 -> "VERBOSE"
            3 -> "DEBUG"
            4 -> "INFO"
            5 -> "WARN"
            6 -> "ERROR"
            else -> "LOG"
        }

//        val logEntry = "$timestamp [$priorityStr] / $tag: $message\n"
        val logEntry = "$message\n"

        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            file.appendText(logEntry)
        } catch (e: Exception) {
            // 靜默處理
        }
    }
}