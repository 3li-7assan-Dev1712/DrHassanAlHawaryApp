package com.example.domain.text

/**
 * A qualification or research line from the About screen, split for display:
 * "بكالوريوس الشريعة - كلية الشريعة ... (1990م)" -> title "بكالوريوس الشريعة",
 * detail "كلية الشريعة ...", date chip "١٩٩٠م", year 1990 (for sorting).
 */
data class DatedEntry(
    val title: String,
    val detail: String?,
    /** The parenthesized date with Arabic-Indic digits ("١٩٩٠م", "نوفمبر ٢٠١٣م"); null if none. */
    val dateLabel: String?,
    val year: Int?,
    /** The text says it isn't published yet ("لم ينشر"). */
    val unpublished: Boolean,
) {
    companion object {
        /** A parenthesized group whose content ends in a 4-digit year + "م": "(1990م)", "(نوفمبر 2013م)". */
        private val DATE = Regex("\\(\\s*([^()]*?([0-9٠-٩]{4})\\s*م)\\s*\\)")
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
                year = match?.groupValues?.get(2)?.let { ArabicNumerals.toWestern(it).toIntOrNull() },
                unpublished = "لم ينشر" in text || "لم يُنشر" in text,
            )
        }
    }
}
