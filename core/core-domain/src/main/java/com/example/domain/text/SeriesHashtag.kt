package com.example.domain.text

/**
 * A series' hashtag from its title, as the admin app suggests it: "شرح صحيح البخاري" ->
 * "#شرح_صحيح_البخاري". Words are joined with "_"; punctuation (Arabic and Latin) is dropped,
 * letters, digits and diacritics are kept.
 */
object SeriesHashtag {

    /** The suggested hashtag, or "" for a title with no letters or digits. */
    fun fromTitle(title: String): String {
        val words = title.split(WHITESPACE)
            .map { word -> word.filter(::isHashtagChar) }
            .filter { it.isNotEmpty() }
        return if (words.isEmpty()) "" else "#" + words.joinToString("_")
    }

    private fun isHashtagChar(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || Character.getType(c) == Character.NON_SPACING_MARK.toInt()

    private val WHITESPACE = Regex("\\s+")
}
