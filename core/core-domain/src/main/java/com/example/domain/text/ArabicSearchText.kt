package com.example.domain.text

/**
 * Client-side, display-only Arabic word matching for search results: which WHOLE words
 * of a title or snippet to highlight, and where to cut a snippet. Never sent to Algolia
 * and never changes its ranking.
 *
 * A word matches when one of its normalized forms starts with a normalized query term:
 * tashkeel and tatweel removed; أ إ آ ٱ -> ا; ة -> ه; ى -> ي; ؤ -> و; ئ -> ي; and the
 * form with one leading prefix (وال بال فال كال لل ال و ف ب ل) stripped. Query terms only
 * lose a leading "ال"; a term shorter than 2 letters is dropped, so "ال" alone matches
 * nothing.
 */
object ArabicSearchText {

    private val PREFIXES = listOf("وال", "بال", "فال", "كال", "لل", "ال", "و", "ف", "ب", "ل")

    /** Letters only, unified spelling (no prefix stripping). */
    fun normalize(word: String): String = buildString(word.length) {
        for (c in word) {
            when (c) {
                in 'ً'..'ْ', 'ٰ', 'ـ' -> Unit
                'أ', 'إ', 'آ', 'ٱ' -> append('ا')
                'ة' -> append('ه')
                'ى' -> append('ي')
                'ؤ' -> append('و')
                'ئ' -> append('ي')
                else -> append(c)
            }
        }
    }

    /** The normalized query words that can match anything (a leading "ال" removed, 2+ letters). */
    fun queryTerms(query: String): List<String> = words(query)
        .map { normalize(query.substring(it.first, it.last + 1)).removePrefix("ال") }
        .filter { it.length >= MIN_TERM }
        .distinct()

    /** False when the query is too short to search ("اكتب كلمة أطول قليلًا"). */
    fun isSearchable(query: String): Boolean = queryTerms(query).isNotEmpty()

    /** True if [word] (one word) matches one of [terms] (from [queryTerms]). */
    fun matches(word: String, terms: List<String>): Boolean {
        if (terms.isEmpty()) return false
        val base = normalize(word)
        val forms = listOf(base) + PREFIXES
            .filter { base.startsWith(it) && base.length - it.length >= MIN_TERM }
            .map { base.removePrefix(it) }
        return forms.any { form -> terms.any { form.startsWith(it) } }
    }

    /** Ranges (inclusive) of the whole words in [text] that match [query]. */
    fun highlightRanges(text: String, query: String): List<IntRange> {
        val terms = queryTerms(query)
        if (terms.isEmpty()) return emptyList()
        return words(text).filter { matches(text.substring(it.first, it.last + 1), terms) }
    }

    /** A snippet plus its highlight ranges (inclusive, in the snippet). */
    data class Snippet(val text: String, val highlights: List<IntRange>)

    /**
     * About [length] characters of [text] around the first matching word, cut at word
     * boundaries, with "…" on each side that was cut. No match: the start of the text.
     */
    fun snippet(text: String, query: String, length: Int = 100): Snippet {
        val clean = text.replace(WHITESPACE, " ").trim()
        val first = highlightRanges(clean, query).firstOrNull()
        var start: Int
        var end: Int
        if (first == null) {
            start = 0
            end = minOf(clean.length, length)
        } else {
            val before = (length - (first.last - first.first + 1)) / 3
            start = (first.first - before).coerceAtLeast(0)
            end = minOf(clean.length, start + length)
            start = (end - length).coerceAtLeast(0).coerceAtMost(start)
        }
        // Never start or end mid-word: drop a partial word at each cut, unless that would
        // cut into the match - then take the whole word instead.
        val keepFrom = first?.first ?: clean.length
        val keepTo = first?.last ?: -1
        if (start > 0 && !clean[start - 1].isWhitespace()) {
            val next = clean.indexOf(' ', start)
            start = if (next in 0 until keepFrom) next + 1 else clean.lastIndexOf(' ', start) + 1
        }
        if (end < clean.length && !clean[end].isWhitespace()) {
            val previous = clean.lastIndexOf(' ', end)
            end = if (previous > maxOf(start, keepTo)) previous else clean.indexOf(' ', end).let { if (it < 0) clean.length else it }
        }
        val body = clean.substring(start, end).trim()
        val lead = if (start > 0) "… " else ""
        val tail = if (end < clean.length) " …" else ""
        val snippetText = lead + body + tail
        return Snippet(snippetText, highlightRanges(snippetText, query))
    }

    /** Ranges (inclusive) of the letter runs in [text]: letters and combining marks. */
    internal fun words(text: String): List<IntRange> {
        val out = mutableListOf<IntRange>()
        var i = 0
        while (i < text.length) {
            if (isWordChar(text[i])) {
                val start = i
                while (i < text.length && isWordChar(text[i])) i++
                out += start until i
            } else {
                i++
            }
        }
        return out
    }

    private fun isWordChar(c: Char) =
        c.isLetterOrDigit() || Character.getType(c) == Character.NON_SPACING_MARK.toInt() || c == 'ـ'

    private const val MIN_TERM = 2
    private val WHITESPACE = Regex("\\s+")
}
