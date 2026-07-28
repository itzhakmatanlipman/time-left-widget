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
 * מצייר את תוכן הווידג'ט כתמונה אחת (bitmap) — גרסה קומפקטית של 3 שורות.
 * הקנבס נמוך יותר כדי שהווידג'ט יתפוס פחות מקום והטקסט יישאר גדול.
 */
object WidgetRenderer {

    private const val W = 600
    private const val H = 230   // גובה נמוך יותר — פחות מקום על המסך

    private const val FONT = 40f  // טקסט גדול וברור

    private val numberFormat = NumberFormat.getInstance(Locale("he", "IL"))

    fun render(r: HeartbeatCalc.Result): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // רקע כהה מעוגל
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E1E1E") }
        c.drawRoundRect(RectF(0f, 0f, W.toFloat(), H.toFloat()), 24f, 24f, bg)

        val mono = Typeface.MONOSPACE
        val monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = mono
            textSize = FONT
        }
        val whiteBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = monoBold
            textSize = FONT
        }

        val padL = 28f
        var y = 52f

        // שורה 1: "estimated [לב] left:" — הלב מצויר ידנית בלבן
        val part1 = "estimated "
        val part2 = " left:"
        c.drawText(part1, padL, y, white)
        val heartX = padL + white.measureText(part1)
        drawHeart(c, heartX, y - FONT * 0.78f, FONT, FONT)
        c.drawText(part2, heartX + FONT * 1.1f, y, white)

        // שורה 2: מספר הפעימות
        y += 56f
        c.drawText(numberFormat.format(r.heartsLeft), padL, y, whiteBold)

        // ---- שורה 3: הבר האופקי ----
        y += 26f
        val barLeft = padL
        val barRight = W - padL
        val barTop = y
        val barBottom = y + 56f
        val barWidth = barRight - barLeft

        val pctLeft = r.pctLeft.coerceIn(0.0, 100.0)
        val split = barLeft + (barWidth * (pctLeft / 100.0)).toFloat()

        val whiteFill = Paint().apply { color = Color.WHITE }
        c.drawRect(barLeft, barTop, split, barBottom, whiteFill)

        val blackFill = Paint().apply { color = Color.BLACK }
        c.drawRect(split, barTop, barRight, barBottom, blackFill)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 2f
        }
        c.drawRect(barLeft, barTop, barRight, barBottom, border)

        val barTextY = barTop + (barBottom - barTop) / 2f + 9f
        val leftText = String.format(Locale.US, "%.2f%%", r.pctLeft)
        val rightText = String.format(Locale.US, "%.2f%%", r.pctPassed)

        val blackText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = mono
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        if (split - barLeft > 100f) {
            c.drawText(leftText, barLeft + (split - barLeft) / 2f, barTextY, blackText)
        }

        val whiteText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = mono
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        if (barRight - split > 100f) {
            c.drawText(rightText, split + (barRight - split) / 2f, barTextY, whiteText)
        }

        return bmp
    }

    /** מצייר לב לבן מלא בתוך קופסה בגודל w×h שפינתה השמאלית-עליונה ב-(x, y). */
    private fun drawHeart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
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
