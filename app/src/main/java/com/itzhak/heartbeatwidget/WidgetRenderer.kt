package com.itzhak.heartbeatwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.text.NumberFormat
import java.util.Locale

/**
 * מצייר את כל תוכן הווידג'ט כתמונה אחת (bitmap).
 * זה נותן שליטה מלאה על העיצוב וזהה למראה של הפתק באובסידיאן.
 */
object WidgetRenderer {

    // רזולוציית ציור קבועה. ה-ImageView בווידג'ט ימתח את זה לגודל האמיתי.
    private const val W = 600
    private const val H = 360

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
        }
        val whiteBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = monoBold
        }

        val padL = 32f
        var y = 56f

        // שורה 1: כותרת מודגשת
        whiteBold.textSize = 34f
        c.drawText("you are going to die", padL, y, whiteBold)

        // שורה 2: תווית פעימות
        y += 60f
        white.textSize = 28f
        c.drawText("estimated \u2665 left :", padL, y, white)

        // שורה 3: מספר הפעימות (מודגש)
        y += 46f
        whiteBold.textSize = 34f
        c.drawText(numberFormat.format(r.heartsLeft), padL, y, whiteBold)

        // שורה 4: תווית אחוז
        y += 58f
        white.textSize = 28f
        c.drawText("estimated life left:", padL, y, white)

        // ---- הבר האופקי ----
        y += 24f
        val barLeft = padL
        val barRight = W - padL
        val barTop = y
        val barBottom = y + 56f
        val barWidth = barRight - barLeft

        val pctLeft = r.pctLeft.coerceIn(0.0, 100.0)
        val split = barLeft + (barWidth * (pctLeft / 100.0)).toFloat()

        // חלק שמאלי לבן (הזמן שנשאר)
        val whiteFill = Paint().apply { color = Color.WHITE }
        c.drawRect(barLeft, barTop, split, barBottom, whiteFill)

        // חלק ימני שחור (הזמן שעבר)
        val blackFill = Paint().apply { color = Color.BLACK }
        c.drawRect(split, barTop, barRight, barBottom, blackFill)

        // מסגרת לבנה דקה סביב כל הבר
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 2f
        }
        c.drawRect(barLeft, barTop, barRight, barBottom, border)

        // טקסט בתוך כל חלק, ממורכז
        val barTextY = barTop + (barBottom - barTop) / 2f + 8f

        val leftText = String.format(Locale.US, "%.2f%%", r.pctLeft)
        val rightText = String.format(Locale.US, "%.2f%%", r.pctPassed)

        // טקסט שחור על הרקע הלבן (רק אם יש מספיק מקום)
        val blackText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = mono
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        if (split - barLeft > 90f) {
            c.drawText(leftText, barLeft + (split - barLeft) / 2f, barTextY, blackText)
        }

        // טקסט לבן על הרקע השחור
        val whiteText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = mono
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        if (barRight - split > 90f) {
            c.drawText(rightText, split + (barRight - split) / 2f, barTextY, whiteText)
        }

        return bmp
    }
}
