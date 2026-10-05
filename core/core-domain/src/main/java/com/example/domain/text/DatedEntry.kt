package com.example.domain.text

/**
 * A qualification or research line from the About screen, split for display:
 * "بكالوريوس الشريعة - كلية الشريعة ... (1990م)" -> title "بكالوريوس الشريعة",
 * detail "كلية الشريعة ...", date chip "١٩٩٠م", year 1990 (for sorting).
 */
data class DatedEntry(
    val title: String,
    val detail: String?,
    /** The parenthesized date with Arabic-Indic digits ("١٩٩٠م", "نوفمبر ٢٠١٣م", "١٤١١هـ"); null if none. */
    val dateLabel: String?,
    /** Gregorian year, for sorting. A Hijri date is converted approximately (1411هـ -> 1990). */
    val year: Int?,
    /** The text says it isn't published yet ("لم ينشر"). */
    val unpublished: Boolean,
) {
    companion object {
        /**
         * A parenthesized group whose content ends in a 4-digit year + "م" (Gregorian) or "هـ"/"ه"
         * (Hijri): "(1990م)", "(نوفمبر 2013م)", "(1411هـ)".
         */
        private val DATE = Regex("\\(\\s*([^()]*?([0-9٠-٩]{4})\\s*(م|هـ|ه))\\s*\\)")
        private val SPLIT = Regex("\\s+[-–—]\\s+")
        private val WHITESPACE = Regex("\\s+")

        fun parse(raw: String): DatedEntry {
            val text = raw.replace(WHITESPACE, " ").trim()
            val match = DATE.findAll(text).lastOrNull()
            val withoutDate = (if (match != null) text.removeRange(match.range) else text)
                .replace(WHITESPACE, " ").trim().trimEnd('-', '–', '—', ' ', '،')
            val parts = withoutDate.split(SPLIT, limit = 2)
            return DatedEntry(
                title = parts[0].trim(),
                detail = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() },
                dateLabel = match?.groupValues?.get(1)?.trim()?.let(ArabicNumerals::digits),
                year = match?.let { m ->
                    val y = ArabicNumerals.toWestern(m.groupValues[2]).toIntOrNull() ?: return@let null
                    if (m.groupValues[3] == "م") y else hijriToGregorian(y)
                },
                unpublished = "لم ينشر" in text || "لم يُنشر" in text,
            )
        }

        /** Good to about a year, which is all sorting needs (a Hijri year is ~0.97 of a solar one). */
        private fun hijriToGregorian(hijriYear: Int): Int = (hijriYear * 0.970229 + 621.5643).toInt()
    }
}
