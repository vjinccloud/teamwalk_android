package com.taiwanlife.teamwalk.ui.login

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.core.graphics.withRotation
import com.google.gson.annotations.SerializedName
import java.security.SecureRandom
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class CaptchaResult(
    @SerializedName("code")
    val code: String,
    @SerializedName("bitmap")
    val bitmap: Bitmap
)

enum class CaptchaMode {
    SIMPLE,        // 僅 0~9
    DIFFICULT      // 0~9 + a~z，排除 0,1,o,i,l
}

object CaptchaGenerator {
    private val simpleCode = "0123456789".toCharArray()
    private val difficultCode = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"//"1234567890abcdefghijklmnopqrstuvwxyz"
        .replace(Regex("[01oilOIL]"), "") // 過濾 0, 1, o, i, l
        .toCharArray()

    private val secureRandom = SecureRandom.getInstance("SHA1PRNG")

    fun generateCaptchaBitmap(
        width: Int = 140,
        height: Int = 35,
        codeLength: Int = 4,
        charSpace: Int = 6,
        textSize: Float = 28f,
        noiseLines: Int = 3,
        mode: CaptchaMode = CaptchaMode.DIFFICULT
    ): CaptchaResult {
        val codeSet = when (mode) {
            CaptchaMode.SIMPLE -> simpleCode
            CaptchaMode.DIFFICULT -> difficultCode
        }

        val code = (1..codeLength).joinToString("") {
            codeSet[secureRandom.nextInt(codeSet.size)].toString()
        }

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.typeface = Typeface.SANS_SERIF
            this.color = Color.BLACK
        }

        // 干擾線
        repeat(noiseLines) {
            paint.color = Color.GRAY
            paint.strokeWidth = 1.5f
            val startX = secureRandom.nextInt(width).toFloat()
            val startY = secureRandom.nextInt(height).toFloat()
            val stopX = secureRandom.nextInt(width).toFloat()
            val stopY = secureRandom.nextInt(height).toFloat()
            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }

        // 繪製文字
        paint.color = Color.BLACK
        val totalCharWidth = codeLength * textSize + (codeLength - 1) * charSpace
        var startX = (width - totalCharWidth) / 2f
        val centerY = (height / 2f) + (textSize / 2f - 4f)

        for (char in code) {
            // 等同於：隨機產生 -25 ~ 25 的整數
            val angle = (secureRandom.nextInt(51) - 25).toFloat()
            canvas.withRotation(angle, startX + textSize / 2f, centerY - textSize / 2f) {
                drawText(char.toString(), startX, centerY, paint)
            }

            startX += textSize + charSpace
        }

        val tcavWaterRippleBitmap = applyWaterRipple(bitmap)
        return CaptchaResult(code, tcavWaterRippleBitmap)
    }

    /**
     * amplitude - 波幅
     * waveFrequency - 波頻
     */
    fun applyWaterRipple(
        source: Bitmap,
        amplitude: Float = 4.0f,
        waveFrequency: Float = 1.2f
    ): Bitmap {
        val width = source.width
        val height = source.height
        val result = createBitmap(width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val offsetX = (amplitude * sin(2.0 * PI * y / (64 / waveFrequency))).toFloat()
                val offsetY = (amplitude * cos(2.0 * PI * x / (64 / waveFrequency))).toFloat()

                val newX = (x + offsetX).roundToInt().coerceIn(0, width - 1)
                val newY = (y + offsetY).roundToInt().coerceIn(0, height - 1)

                result[x, y] = source[newX, newY]
            }
        }

        return result
    }
}
