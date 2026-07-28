package com.itzhak.heartbeatwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.text.NumberFormat
import java.util.Locale

/**
 * מצייר את תוכן הווידג'ט כתמונה אחת (bitmap) — 3 שורות, רקע שחור.
 * רוחב הבר מתאים לרוחב השורה הראשונה. רוחב הקנבס מחושב לפי התוכן.
 */
object WidgetRenderer {

    private const val FONT = 26f                 // גודל טקסט אחיד לכל השורות
    private const val HEART = 20f                // גודל הלב (מעט קטן מהטקסט)
    private const val H = 168                     // גובה קבוע

    private val numberFormat = NumberFormat.getInstance(Locale("he", "IL"))

    fun render(r: HeartbeatCalc.Result): Bitmap {
        val mono = Typeface.MONOSPACE
        val monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; typeface = mono; textSize = FONT
        }
        val whiteBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; typeface = monoBold; textSize = FONT
        }

        val padL = 28f
        val spaceW = white.measureText(" ")   // רוחב רווח אחד

        // מדידת השורה הראשונה: "estimated" + רווח + לב + רווח + "left:"
        val wEstimated = white.measureText("estimated")
        val wLeft = white.measureText("left:")
        val line1Right = padL + wEstimated + spaceW + HEART + spaceW + wLeft

        // רוחב הקנבס = תוכן + שוליים
        val W = Math.ceil((line1Right + padL).toDouble()).toInt()

        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // רקע שחור מעוגל
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
        c.drawRoundRect(RectF(0f, 0f, W.toFloat(), H.toFloat()), 24f, 24f, bg)

        // ---- שורה 1 ----
        var y = 40f
        var x = padL
        c.drawText("estimated", x, y, white)
        x += wEstimated + spaceW
        drawHeart(c, x, y - FONT * 0.35f - HEART * 0.45f, HEART, HEART)
        x += HEART + spaceW
        c.drawText("left:", x, y, white)

        // ---- שורה 2: מספר הפעימות ----
        y = 78f
        c.drawText(numberFormat.format(r.heartsLeft), padL, y, whiteBold)

        // ---- שורה 3: הבר (ברוחב השורה הראשונה) ----
        val barLeft = padL
        val barRight = line1Right
        val barTop = 96f
        val barBottom = 142f
        val barWidth = barRight - barLeft

        val pctLeft = r.pctLeft.coerceIn(0.0, 100.0)
        val split = barLeft + (barWidth * (pctLeft / 100.0)).toFloat()

        val whiteFill = Paint().apply { color = Color.WHITE }
        c.drawRect(barLeft, barTop, split, barBottom, whiteFill)

        val blackFill = Paint().apply { color = Color.BLACK }
        c.drawRect(split, barTop, barRight, barBottom, blackFill)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; color = Color.WHITE; strokeWidth = 2f
        }
        c.drawRect(barLeft, barTop, barRight, barBottom, border)

        val barTextY = barTop + (barBottom - barTop) / 2f + 9f
        val leftText = String.format(Locale.US, "%.2f%%", r.pctLeft)
        val rightText = String.format(Locale.US, "%.2f%%", r.pctPassed)

        val blackText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; typeface = mono; textSize = FONT; textAlign = Paint.Align.CENTER
        }
        if (split - barLeft > 90f) {
            c.drawText(leftText, barLeft + (split - barLeft) / 2f, barTextY, blackText)
        }

        val whiteText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; typeface = mono; textSize = FONT; textAlign = Paint.Align.CENTER
        }
        if (barRight - split > 90f) {
            c.drawText(rightText, split + (barRight - split) / 2f, barTextY, whiteText)
        }

        return bmp
    }

    /** לב לבן מלא בתוך קופסה בגודל w×h שפינתה השמאלית-עליונה ב-(x, y). */
    private fun drawHeart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
        val path = Path()
        path.moveTo(x + w * 0.5f, y + h * 0.85f)
        path.cubicTo(x + w * 0.05f, y + h * 0.55f, x + w * 0.05f, y + h * 0.10f, x + w * 0.35f, y + h * 0.10f)
        path.cubicTo(x + w * 0.45f, y + h * 0.10f, x + w * 0.50f, y + h * 0.20f, x + w * 0.50f, y + h * 0.28f)
        path.cubicTo(x + w * 0.50f, y + h * 0.20f, x + w * 0.55f, y + h * 0.10f, x + w * 0.65f, y + h * 0.10f)
        path.cubicTo(x + w * 0.95f, y + h * 0.10f, x + w * 0.95f, y + h * 0.55f, x + w * 0.5f, y + h * 0.85f)
        path.close()
        canvas.drawPath(path, paint)
    }
}
