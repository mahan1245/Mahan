package com.example.util

import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object PersianUtils {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun toPersianDigits(text: Any?): String {
        if (text == null) return ""
        val s = text.toString()
        val builder = StringBuilder()
        for (ch in s) {
            when (ch) {
                in '0'..'9' -> builder.append(PERSIAN_DIGITS[ch - '0'])
                in '٠'..'٩' -> builder.append(PERSIAN_DIGITS[ch - '٠'])
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun toEnglishDigits(text: String): String {
        val builder = StringBuilder()
        for (ch in text) {
            when (ch) {
                in '۰'..'۹' -> builder.append(('0'.code + (ch - '۰')).toChar())
                in '٠'..'٩' -> builder.append(('0'.code + (ch - '٠')).toChar())
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    val SHAMSI_MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    val WEEKDAY_NAMES = listOf(
        "شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        fun formatShamsi(): String {
            val monthName = SHAMSI_MONTH_NAMES.getOrElse(month - 1) { "" }
            return toPersianDigits("$day $monthName $year")
        }

        fun formatShort(): String {
            val m = if (month < 10) "0$month" else "$month"
            val d = if (day < 10) "0$day" else "$day"
            return "$year-$m-$d"
        }
    }

    /**
     * Converts Gregorian date to Jalali (Shamsi) date.
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400)
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy2 % 4 == 0 && gy2 % 100 != 0) || (gy2 % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += ((jDayNo - 1) / 365)
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        val jd = jDayNo + 1

        return JalaliDate(jy, jm + 1, jd)
    }

    fun currentJalaliDate(): JalaliDate {
        val calendar = Calendar.getInstance()
        return gregorianToJalali(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getWeekdayName(calendar: Calendar = Calendar.getInstance()): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یک‌شنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> "شنبه"
        }
    }

    fun formatDurationPersian(minutes: Int): String {
        if (minutes <= 0) return "۰ دقیقه"
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> toPersianDigits("$hours ساعت و $mins دقیقه")
            hours > 0 -> toPersianDigits("$hours ساعت")
            else -> toPersianDigits("$mins دقیقه")
        }
    }

    fun formatSecondsToTime(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) {
            toPersianDigits(String.format(Locale.US, "%02d:%02d:%02d", h, m, s))
        } else {
            toPersianDigits(String.format(Locale.US, "%02d:%02d", m, s))
        }
    }

    /**
     * Konkur test percentage formula:
     * Percentage = ((Correct * 3) - Wrong) / (Total * 3) * 100
     */
    fun calculateKonkurPercentage(correct: Int, wrong: Int, total: Int): Double {
        if (total <= 0) return 0.0
        val raw = ((correct * 3.0) - wrong) / (total * 3.0) * 100.0
        return (raw * 10.0).roundToInt() / 10.0
    }
}
