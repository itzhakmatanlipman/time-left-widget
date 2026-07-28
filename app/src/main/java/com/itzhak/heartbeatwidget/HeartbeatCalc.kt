package com.itzhak.heartbeatwidget

import java.util.Calendar

/**
 * כל החישוב במקום אחד.
 * ==== ההגדרות שאתה משנה נמצאות כאן למעלה ====
 */
object HeartbeatCalc {

    // ---- הגדרות ----
    const val BIRTH_YEAR = 1982
    const val BIRTH_MONTH = 10   // אוקטובר
    const val BIRTH_DAY = 3
    const val BIRTH_HOUR = 6     // שש בבוקר
    const val BIRTH_MINUTE = 0

    const val BPM = 61.0                 // דופק ממוצע לדקה
    const val LIFE_EXPECTANCY_YEARS = 86  // תוחלת חיים בשנים
    // -----------------

    data class Result(
        val heartsLeft: Long,   // פעימות שנותרו
        val pctPassed: Double,  // אחוז חיים שעבר
        val pctLeft: Double     // אחוז חיים שנשאר
    )

    private fun birthMillis(): Long {
        val c = Calendar.getInstance()
        // Calendar.MONTH הוא 0-מבוסס, לכן פחות 1
        c.set(BIRTH_YEAR, BIRTH_MONTH - 1, BIRTH_DAY, BIRTH_HOUR, BIRTH_MINUTE, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun deathMillis(): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = birthMillis()
        val whole = LIFE_EXPECTANCY_YEARS.toInt()
        val fraction = LIFE_EXPECTANCY_YEARS - whole
        c.add(Calendar.YEAR, whole)
        // מוסיפים את החלק העשרוני של השנה כימים (365.25 כדי לכלול שנים מעוברות)
        c.add(Calendar.DAY_OF_YEAR, Math.round(fraction * 365.25).toInt())
        return c.timeInMillis
    }

    fun compute(): Result {
        val now = System.currentTimeMillis()
        val birth = birthMillis()
        val death = deathMillis()

        val bps = BPM / 60.0
        val secLived = (now - birth) / 1000.0
        val secTotal = (death - birth) / 1000.0

        val passed = secLived * bps
        val total = secTotal * bps
        val left = total - passed

        val pctPassed = passed / total * 100.0
        val pctLeft = 100.0 - pctPassed

        return Result(
            heartsLeft = left.toLong(),
            pctPassed = pctPassed,
            pctLeft = pctLeft
        )
    }
}
