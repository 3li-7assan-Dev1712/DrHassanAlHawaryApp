package com.example.domain.text

import java.util.Locale

/**
 * The ONE place numbers on the share card are formatted. Everything burned into
 * the frame - dates, times, digits inside titles - goes through here, so the
 * card never mixes Arabic-Indic (٢٧) with Western (2026) digits again.
 *
 * Pure Kotlin (no ICU, Locale.ROOT formatting), so output is identical on every device and
 * covered by JVM unit tests.
 */
object ArabicNumerals {

    internal val GREGORIAN_MONTHS = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر",
    )

    /** Middle dot between the Hijri and Gregorian dates, as in the design. */
    const val DATE_SEPARATOR = " · "

    /** Every Western (0-9) and Persian (۰-۹) digit -> Arabic-Indic (٠-٩); all else untouched. */
    fun digits(text: String): String = buildString(text.length) {
        for (c in text) {
            append(
                when (c) {
                    in '0'..'9' -> '٠' + (c - '0')
                    in '۰'..'۹' -> '٠' + (c - '۰')
                    else -> c
                }
            )
        }
    }

    fun digits(number: Int): String = digits(number.toString())

    /** Arabic-Indic / Persian -> Western digits, for parsing. */
    fun toWestern(text: String): String = buildString(text.length) {
        for (c in text) {
            append(
                when (c) {
                    in '٠'..'٩' -> '0' + (c - '٠')
                    in '۰'..'۹' -> '0' + (c - '۰')
                    else -> c
                }
            )
        }
    }

    /** "٢٧ ذو القعدة ١٤٤٧هـ" */
    fun formatHijri(date: HijriDate): String = "${digits(date.day)} ${date.monthName} ${digits(date.year)}هـ"

    /** "١٥ مايو ٢٠٢٦م" */
    fun formatGregorian(date: GregorianDate): String =
        "${digits(date.day)} ${GREGORIAN_MONTHS[(date.month - 1).coerceIn(0, 11)]} ${digits(date.year)}م"

    /** "٢٧ ذو القعدة ١٤٤٧هـ · ١٥ مايو ٢٠٢٦م", or whichever half exists; null if neither. */
    fun formatDateLine(hijri: HijriDate?, gregorian: GregorianDate?): String? {
        val parts = listOfNotNull(hijri?.let(::formatHijri), gregorian?.let(::formatGregorian))
        return parts.takeIf { it.isNotEmpty() }?.joinToString(DATE_SEPARATOR)
    }

    /**
     * Media player / share-clip times: no leading zero on minutes, Arabic-Indic digits.
     * "٠:٢٠", "٣:٠٩", "٢٢:٠٠", and "١:٠٥:٣٠" past an hour. Negative input clamps to zero.
     */
    fun formatMediaTime(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        val western = if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
        }
        return digits(western)
    }

    /** "٠١:٠٥", or "١:٠٢:٠٥" past an hour. Negative input clamps to zero. */
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        val western = if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
        return digits(western)
    }
}
