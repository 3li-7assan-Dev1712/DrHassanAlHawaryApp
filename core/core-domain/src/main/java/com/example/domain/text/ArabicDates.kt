package com.example.domain.text

/**
 * Relative dates and counted units in Arabic, with correct number agreement:
 * 1 -> the singular alone ("دقيقة"), 2 -> the dual ("دقيقتين"), 3-10 -> number +
 * plural ("٣ دقائق"), 11+ -> number + singular ("١١ دقيقة"). All digits go
 * through [ArabicNumerals], so they're Arabic-Indic.
 */
object ArabicDates {

    /** The forms of one unit: singular, dual (genitive, as after "منذ"/"قراءة"), plural (3-10), and 11+. */
    class Unit(val singular: String, val dual: String, val plural: String, val many: String = singular)

    val MINUTE = Unit("دقيقة", "دقيقتين", "دقائق")
    val HOUR = Unit("ساعة", "ساعتين", "ساعات")
    val DAY = Unit("يوم", "يومين", "أيام", many = "يومًا")
    val WEEK = Unit("أسبوع", "أسبوعين", "أسابيع", many = "أسبوعًا")
    val MONTH = Unit("شهر", "شهرين", "أشهر", many = "شهرًا")
    val YEAR = Unit("سنة", "سنتين", "سنوات")

    /** "دقيقة", "دقيقتين", "٣ دقائق", "١١ دقيقة". */
    fun count(n: Long, unit: Unit): String = when {
        n <= 1L -> unit.singular
        n == 2L -> unit.dual
        n in 3L..10L -> "${ArabicNumerals.digits(n.toString())} ${unit.plural}"
        else -> "${ArabicNumerals.digits(n.toString())} ${unit.many}"
    }

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

    private const val MINUTE_MS = 60_000L
}
