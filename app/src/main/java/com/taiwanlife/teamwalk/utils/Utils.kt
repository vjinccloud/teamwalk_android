package com.taiwanlife.teamwalk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Base64
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.PatternLockView.Dot
import timber.log.Timber
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
}