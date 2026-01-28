package com.taiwanlife.teamwalk.utils

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.PatternLockView.Dot
import com.taiwanlife.teamwalk.Config
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Utils {

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * Generates a random string of a fixed length containing
     * alphanumeric characters (a-z, A-Z, 0-9).
     *
     * @return A randomly generated string.
     */
    fun randomString(targetStringLength: Int): String {
        val letter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val secureRandom = SecureRandom()
        val sb = StringBuilder()

        for (i in 0 until targetStringLength) {
            val randomInt = secureRandom.nextInt(letter.length)
            sb.append(letter[randomInt]) // Kotlin allows direct indexing on String for char
        }
        return sb.toString()
    }

    /**
     * 使用 HmacSHA1 演算法對給定的字串進行 SHA1 加密，並使用提供的密鑰。
     * 結果會以 Base64 編碼的字串形式返回。
     *
     * @param s 要加密的原始字串。
     * @param keyString 用於 HMAC 加密的密鑰字串。
     * @return 經過 HmacSHA1 加密並 Base64 編碼後的字串。
     * @throws UnsupportedEncodingException 如果 UTF-8 編碼不受支持。
     * @throws NoSuchAlgorithmException 如果 HmacSHA1 演算法不可用。
     * @throws InvalidKeyException 如果提供的密鑰無效。
     */
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class
    )
    fun sha1(s: String, keyString: String): String {
        // 將密鑰字串和原始字串轉換為 UTF-8 字節數組
        // SecretKeySpec 用於創建加密密鑰
        val key = SecretKeySpec(keyString.toByteArray(charset("UTF-8")), "HmacSHA1")

        // 獲取 HmacSHA1 演算法的 Mac 實例
        val mac = Mac.getInstance("HmacSHA1")
        // 使用創建的密鑰初始化 Mac 實例
        mac.init(key)

        // 對原始字串的 UTF-8 字節數組進行加密
        val bytes = mac.doFinal(s.toByteArray(charset("UTF-8")))

        // 將結果字節數組進行 Base64 編碼 (不包含換行符)，然後轉換為字串返回
        // Base64.NO_WRAP 確保輸出字串中沒有換行符，通常在傳輸或儲存時使用
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun patternToSha256(
        patternLockView: PatternLockView,
        pattern: MutableList<Dot>, fid: String
    ): String {
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(
                patternToString(
                    patternLockView,
                    pattern,
                    fid
                ).toByteArray(charset("UTF-8"))
            )
            Timber.d(
                "patternToSha256 toStr: ${
                    patternToString(
                        patternLockView,
                        pattern,
                        fid
                    )
                }"
            )
            val digest = messageDigest.digest()
            val bigInteger = BigInteger(1, digest)
            return String.format(
                null as Locale?,
                "%0" + (digest.size * 2) + "x", bigInteger
            ).lowercase(Locale.getDefault())
        } catch (e: NoSuchAlgorithmException) {
            return ""
        } catch (e: UnsupportedEncodingException) {
            return ""
        }
    }

    fun patternToString(
        patternLockView: PatternLockView?,
        pattern: MutableList<Dot>?, fid: String
    ): String {
        if (pattern == null) {
            return ""
        }
        val patternSize = pattern.size
        val stringBuilder = java.lang.StringBuilder()

        for (i in 0..<patternSize) {
            val dot = pattern.get(i)
            //            stringBuilder.append((patternSize * patternLockView.getDotCount() + patternSize));
            stringBuilder.append(dot.row)
            stringBuilder.append(dot.column)
        }
        return stringBuilder.toString() + fid
    }

    /**
     * 將 Date 物件格式化為指定字串，或回傳當前時間的預設格式字串。
     *
     * @param date 要格式化的 Date 物件。如果為 null，則使用當前時間。
     * @param format 格式字串，例如 "yyyy/MM/dd HH:mm:ss"。如果為 null，則使用預設格式。
     * @return 格式化後的日期時間字串。
     */
    fun formatDate(date: Date? = null, format: String? = "yyyy/MM/dd HH:mm:ss"): String {
        // 如果 date 為 null，則使用當前時間
        val dateToFormat = date ?: Date()

        // 建立 SimpleDateFormat 物件，並設定語系為當前系統預設
        val formatter = SimpleDateFormat(format, Locale.getDefault())

        // 格式化並回傳
        return formatter.format(dateToFormat)
    }

    /**
     * 將 LocalDateTime 物件格式化為指定字串，或回傳當前時間的預設格式字串。
     *
     * @param dateTime 要格式化的 LocalDateTime 物件。如果為 null，則使用當前時間。
     * @param format 格式字串，例如 "yyyy/MM/dd HH:mm:ss"。如果為 null，則使用預設格式。
     * @return 格式化後的日期時間字串。
     */
    fun formatLocalDateTime(
        dateTime: LocalDateTime? = null,
        format: String? = "yyyy/MM/dd HH:mm:ss"
    ): String {
        // 如果 dateTime 為 null，則使用當前 LocalDateTime
        val dateTimeToFormat = dateTime ?: LocalDateTime.now()

        // 建立 DateTimeFormatter 物件
        val formatter = DateTimeFormatter.ofPattern(format)

        // 格式化並回傳
        return dateTimeToFormat.format(formatter)
    }

    /**
     * 能計算包含中文字的數量
     */
    fun calculateChineseLength(c: CharSequence): Int {
        var len = 0
        val l = c.length
        for (i in 0..<l) {
            val tmp = c.get(i)
            if (tmp.code >= 0x20 && tmp.code <= 0x7E) {
                len++
            } else {
                len += 2
            }
        }
        return len
    }

//    /**
//     * 把 base64 圖片分享到其他 App（把檔案放在 app cache，使用 FileProvider 分享）
//     *
//     * @param context Application or Activity context
//     * @param base64OrDataUri 可能是純 base64，也可能是 data:image/png;base64,.... 的格式
//     * @param suggestedFileName  建議檔名（含副檔名），若為 null 會自動產生 .png
//     */
//    fun shareBase64ImageSecure(
//        context: Context,
//        base64OrDataUri: String,
//        suggestedFileName: String = "share_image.png"
//    ) {
//        try {
//            val (mimeType, base64Str) = if (base64OrDataUri.startsWith("data:")) {
//                // data:[<mediatype>][;base64],<data>
//                val parts = base64OrDataUri.split(",")
//                val meta = parts.getOrNull(0) ?: ""
//                val body = parts.getOrNull(1) ?: ""
//                val mt = meta.substringAfter("data:", "image/png").substringBefore(";")
//                Pair(mt, body)
//            } else {
//                Pair("image/png", base64OrDataUri)
//            }
//
//            val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
//            val bitmap: Bitmap? = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//
//            // 寫入 app 的 cache 目錄（非公開）
//            val imagesDir = File(context.cacheDir, "pictures").apply { if (!exists()) mkdirs() }
//            val ext = when {
//                mimeType.contains("png") -> "png"
//                mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
//                else -> "png"
//            }
//            val fileName = suggestedFileName
//            val outFile = File(imagesDir, fileName)
//
//            FileOutputStream(outFile).use { fos ->
//                if (bitmap != null) {
//                    val format =
//                        if (ext == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
//                    bitmap.compress(format, 100, fos)
//                } else {
//                    // fallback: 如果不能 decode 成 bitmap，就直接把原 bytes 寫入（適用於已是 png/jpg bytes）
//                    fos.write(imageBytes)
//                }
//                fos.flush()
//            }
//
//            val authority = "${context.packageName}.fileprovider"
//            val imageUri: Uri = FileProvider.getUriForFile(context, authority, outFile)
//
//            val sendIntent = Intent(Intent.ACTION_SEND).apply {
//                putExtra(Intent.EXTRA_STREAM, imageUri)
//                type = mimeType.ifEmpty { "image/*" }
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//                // ClipData 可確保 chooser/preview 可以正確取得 URI（避免部分裝置 preview 權限問題）
//                clipData = ClipData(
//                    ClipDescription("image", arrayOf("image/*")),
//                    ClipData.Item(imageUri)
//                )
//            }
//
//            val chooser = Intent.createChooser(sendIntent, "分享圖片")
//
//            val resInfoList = context.packageManager.queryIntentActivities(
//                sendIntent,
//                PackageManager.MATCH_DEFAULT_ONLY
//            )
//            resInfoList.forEach { resolveInfo ->
//                val packageName = resolveInfo.activityInfo.packageName
//                try {
//                    context.grantUriPermission(
//                        packageName,
//                        imageUri,
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    )
//                } catch (_: Exception) { /* ignore */
//                }
//            }
//
//            context.startActivity(chooser)
//
//            // 60秒之後刪除
////            Handler(Looper.getMainLooper()).postDelayed({
////                try {
////                    outFile.delete()
////                    if (imagesDir.exists() && imagesDir.listFiles()?.isEmpty() == true) imagesDir.delete()
////                } catch (_: Exception) { /* ignore */ }
////            }, 60_000L) // 60s 後刪除
//        } catch (e: Exception) {
//            e.printStackTrace()
//
//            context.debugToast("分享圖片失敗")
//        }
//    }

    fun shareBase64ImageSecure(
        context: Context,
        base64OrDataUri: String,
        suggestedFileName: String = "share_image.png"
    ) {
        try {
            val (mimeType, base64Str) = if (base64OrDataUri.startsWith("data:")) {
                val parts = base64OrDataUri.split(",")
                val meta = parts.getOrNull(0) ?: ""
                val body = parts.getOrNull(1) ?: ""
                val mt = meta.substringAfter("data:", "image/png").substringBefore(";")
                Pair(mt, body)
            } else {
                Pair("image/png", base64OrDataUri)
            }

            val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)

            val imagesDir = File(context.cacheDir, "pictures").apply { if (!exists()) mkdirs() }
            val outFile = File(imagesDir, suggestedFileName)

            FileOutputStream(outFile).use { fos ->
                fos.write(imageBytes)
                fos.flush()
            }

            val authority = "${context.packageName}.fileprovider"
            val imageUri: Uri = FileProvider.getUriForFile(context, authority, outFile)

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = mimeType.ifEmpty { "image/png" } // 確保預設是 png 以支援透明
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData(
                    ClipDescription("image", arrayOf("image/*")),
                    ClipData.Item(imageUri)
                )
            }

            val chooser = Intent.createChooser(sendIntent, "分享圖片")

            // 針對 Android 10+ 以外的權限處理 (Optional)
            val resInfoList = context.packageManager.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)
            resInfoList.forEach { resolveInfo ->
                context.grantUriPermission(resolveInfo.activityInfo.packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            // context.debugToast("分享圖片失敗")
        }
    }

    fun extractTicketFromUrl(urlString: String, paramName: String): String? {
        try {
            val uri = urlString.toUri()

            if (uri.isHierarchical && uri.query != null) {

                val ticketValue = uri.getQueryParameter(paramName)

                // 檢查取出的值是否非空且非空白
                if (!ticketValue.isNullOrEmpty()) {
                    // Timber.d("URL_CHECK", "Found ticket: $ticketValue")
                    return ticketValue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun stringToNotificationType(value: String?): Config.NotificationType {
        return when (value) {
            Config.NotificationType.URL.v -> Config.NotificationType.URL
            Config.NotificationType.APP_PAGE.v -> Config.NotificationType.APP_PAGE
            else -> Config.NotificationType.NONE
        }
    }
}