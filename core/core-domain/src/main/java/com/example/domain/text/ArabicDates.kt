package com.example.domain.text

import java.util.Calendar
import java.util.TimeZone

/**
 * Relative dates and counted units in Arabic, with correct number agreement:
 * 1 -> the singular alone ("دقيقة"), 2 -> the dual ("دقيقتين"), 3-10 -> number +
 * plural ("٣ دقائق"), 11+ -> number + singular ("١١ دقيقة"). All digits go
 * through [ArabicNumerals], so they're Arabic-Indic.
 */
object ArabicDates {

    /**
     * The forms of one unit, per the Arabic plural categories (CLDR): [singular] for 1,
     * [dual] for 2 (genitive, as after "منذ"/"قراءة"), [plural] for 3-10, [many] for
     * 11-99, and [other] for 0 and 100, 101, 102, 200… (the last two digits decide: 103 is
     * "few" again, 111 "many").
     */
    class Unit(
        val singular: String,
        val dual: String,
        val plural: String,
        val many: String = singular,
        val other: String = singular,
    )

    val MINUTE = Unit("دقيقة", "دقيقتين", "دقائق")
    val HOUR = Unit("ساعة", "ساعتين", "ساعات")
    val DAY = Unit("يوم", "يومين", "أيام", many = "يومًا")
    val WEEK = Unit("أسبوع", "أسبوعين", "أسابيع", many = "أسبوعًا")
    val MONTH = Unit("شهر", "شهرين", "أشهر", many = "شهرًا")
    val YEAR = Unit("سنة", "سنتين", "سنوات")

    /** "دقيقة", "دقيقتين", "٣ دقائق", "١١ دقيقة", "١٠٠ دقيقة", "١٠٣ دقائق". */
    fun count(n: Long, unit: Unit): String {
        val number = ArabicNumerals.digits(n.toString())
        return when {
            n == 1L || n < 0L -> unit.singular
            n == 2L -> unit.dual
            n % 100 in 3L..10L -> "$number ${unit.plural}"
            n % 100 in 11L..99L -> "$number ${unit.many}"
            else -> "$number ${unit.other}" // 0, 100-102, 200-202…
        }
    }

    /** Characters in a quote: "حرف واحد", "حرفان", "٣ أحرف", "٩٨ حرفًا", "١٠٠ حرف", "٢١٢ حرفًا". */
    val CHARACTER = Unit(singular = "حرف واحد", dual = "حرفان", plural = "أحرف", many = "حرفًا", other = "حرف")

    /** Images of a quote: "صورة واحدة", "صورتان", "٣ صور", "١١ صورة". */
    val IMAGE = Unit(singular = "صورة واحدة", dual = "صورتان", plural = "صور", many = "صورة", other = "صورة")

    /** "قراءة ٧ دقائق" */
    fun readingTime(minutes: Int): String = "قراءة ${count(minutes.toLong().coerceAtLeast(1), MINUTE)}"

    /**
     * "الآن", "منذ دقيقة", "منذ ٣ ساعات", "أمس", "منذ يومين", "منذ ٣ أيام", "منذ أسبوع",
     * "منذ شهرين", "منذ سنة"... A time in the future (clock skew) reads as "الآن".
     */
    fun relative(nowMillis: Long, thenMillis: Long): String {
        val minutes = (nowMillis - thenMillis) / MINUTE_MS
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "الآن"
            minutes < 60 -> "منذ ${count(minutes, MINUTE)}"
            hours < 24 -> "منذ ${count(hours, HOUR)}"
            days == 1L -> "أمس"
            days < 7 -> "منذ ${count(days, DAY)}"
            days < 30 -> "منذ ${count(days / 7, WEEK)}"
            days < 365 -> "منذ ${count(days / 30, MONTH)}"
            else -> "منذ ${count(days / 365, YEAR)}"
        }
    }

    /**
     * The whole "published" phrase for list meta lines - callers must not add their own
     * "نُشر"/"منذ": "نُشر الآن", "نُشر منذ ٣ دقائق", "نُشر أمس", "نُشر منذ أسبوعين", and past
     * 30 days the calendar date, "نُشر في ٤ أبريل ٢٠٢٦". A future time reads as "نُشر الآن".
     */
    fun published(nowMillis: Long, thenMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val minutes = (nowMillis - thenMillis) / MINUTE_MS
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "نُشر الآن"
            minutes < 60 -> "نُشر منذ ${count(minutes, MINUTE)}"
            hours < 24 -> "نُشر منذ ${count(hours, HOUR)}"
            days == 1L -> "نُشر أمس"
            days < 7 -> "نُشر منذ ${count(days, DAY)}"
            days <= 30 -> "نُشر منذ ${count(days / 7, WEEK)}"
            else -> "نُشر في ${calendarDate(thenMillis, timeZone)}"
        }
    }

    /** "٤ أبريل ٢٠٢٦" */
    fun calendarDate(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val c = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
        val month = ArabicNumerals.GREGORIAN_MONTHS[c.get(Calendar.MONTH)]
        return "${ArabicNumerals.digits(c.get(Calendar.DAY_OF_MONTH))} $month ${ArabicNumerals.digits(c.get(Calendar.YEAR))}"
    }

    private const val MINUTE_MS = 60_000L
}
