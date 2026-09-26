package com.example.core.ui.util

import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone
import com.example.domain.text.ArabicCalendarNames
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.HijriDate
import java.util.Calendar
import java.util.Date

/**
 * Gregorian -> Hijri using ICU's Umm al-Qura calendar (android.icu, API 24+).
 *
 * Umm al-Qura is computed, so it can differ by a day from a date announced
 * locally after moon-sighting. Where the content itself states its Hijri date
 * (e.g. a khutba title), prefer that - see ShareTitleParser.
 */
object HijriCalendar {

    fun hijriDate(date: Date): HijriDate {
        val calendar = IslamicCalendar(TimeZone.getDefault()).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            time = date
        }
        return HijriDate(
            day = calendar.get(IslamicCalendar.DAY_OF_MONTH),
            monthName = ArabicCalendarNames.hijriMonths[calendar.get(IslamicCalendar.MONTH).coerceIn(0, 11)],
            year = calendar.get(IslamicCalendar.YEAR),
        )
    }

    /** "السبت ١٤ ربيع الآخر ١٤٤٨هـ" */
    fun formatWithWeekday(date: Date): String {
        val weekday = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK)
        return "${ArabicCalendarNames.weekdays[weekday - 1]} ${ArabicNumerals.formatHijri(hijriDate(date))}"
    }
}
