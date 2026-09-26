package com.example.domain.text

import java.text.BreakIterator
import java.util.Locale

/** A cleaned excerpt, plus where an optional highlight landed inside it (end exclusive). */
data class SanitizedQuote(
    val text: String,
    val highlightStart: Int? = null,
    val highlightEnd: Int? = null,
)

/**
 * Turns a raw selection of article text into something fit to put on a card.
 *
 * Article bodies are scraped from Facebook posts, and a drag-selection rarely
 * lands exactly on word or sentence edges, so:
 *  - selection edges snap outward to whole words, using the FULL article text
 *    (a selection ending in "سودا" becomes "سوداني");
 *  - emojis, tatweel and separator lines ("=====", "ـــــ", "•••") are removed;
 *  - "..." becomes "…", runs of spaces collapse, and paragraph breaks collapse
 *    to a single newline;
 *  - stray punctuation is trimmed from both ends - the previous sentence's full
 *    stop at the start is what RTL layout moved to the front of the old card -
 *    but sentence-final punctuation at the end is kept;
 *  - unmatched brackets and quotes are dropped, so there's never an unclosed "(";
 *  - text that doesn't end a sentence gets a trailing "…" (never a leading one,
 *    which looks cluttered in RTL).
 *
 * Every step keeps a map back to the source offsets so a highlight range given
 * in article coordinates can be carried onto the cleaned text. Pure Kotlin
 * (java.text only), covered by JVM unit tests.
 */
object TextSanitizer {

    const val ELLIPSIS = '…'
    private const val SENTENCE_FINAL = ".؟?!…۔"
    private const val STRAY = ".،,؛;:-–—«»\"'()[]{}“”‘’‹›۔؟?!…"
    private const val SEPARATOR_CHARS = "=-_*•ـ~"
    private val PAIRS = mapOf('(' to ')', '[' to ']', '{' to '}', '«' to '»', '“' to '”', '‹' to '›')

    fun sanitize(
        fullText: String,
        selectionStart: Int,
        selectionEnd: Int,
        highlight: IntRange? = null,
    ): SanitizedQuote {
        var start = selectionStart.coerceIn(0, fullText.length)
        var end = selectionEnd.coerceIn(start, fullText.length)
        if (start == end) return SanitizedQuote("")

        val words = BreakIterator.getWordInstance(Locale("ar")).apply { setText(fullText) }
        if (!words.isBoundary(start)) start = words.preceding(start).coerceAtLeast(0)
        if (end < fullText.length && !words.isBoundary(end)) {
            end = words.following(end).let { if (it == BreakIterator.DONE) fullText.length else it }
        }

        var text = Mapped.of(fullText, start, end)
        text = removeEmojisAndTatweel(text)
        text = removeLines(text, ArticleText::isFacebookByline)
        text = removeSeparatorLines(text)
        text = normalizeEllipses(text)
        text = collapseWhitespace(text)
        text = trimEnds(text)
        text = removeUnmatchedPairs(text)
        text = trimEnds(text)
        if (text.isEmpty()) return SanitizedQuote("")
        if (text.last() !in SENTENCE_FINAL) text = text.append(ELLIPSIS)

        val (hStart, hEnd) = highlight?.let { mapHighlight(text, it) } ?: (null to null)
        return SanitizedQuote(text.toString(), hStart, hEnd)
    }

    /** First/last cleaned chars whose source offsets fall inside [range] (inclusive source range). */
    private fun mapHighlight(text: Mapped, range: IntRange): Pair<Int?, Int?> {
        val first = text.src.indexOfFirst { it in range }
        val last = text.src.indexOfLast { it in range }
        return if (first < 0 || last < first) null to null else first to last + 1
    }

    private fun removeEmojisAndTatweel(text: Mapped): Mapped {
        val out = Mapped()
        var i = 0
        while (i < text.length) {
            val cp = Character.codePointAt(text.chars, i)
            val width = Character.charCount(cp)
            if (!isEmojiOrJoiner(cp) && cp != 0x0640) {
                for (k in 0 until width) out.add(text.chars[i + k], text.src[i + k])
            }
            i += width
        }
        return out
    }

    internal fun isEmojiOrJoiner(cp: Int): Boolean =
        cp in 0x1F000..0x1FAFF || // pictographs, emoticons, transport, flags, supplemental symbols
            cp in 0x2600..0x27BF || // misc symbols + dingbats
            cp in 0x2B00..0x2BFF || // stars, arrows
            cp in 0x2300..0x23FF || // misc technical (⌚ ⏰ ...)
            cp in 0xFE00..0xFE0F || // variation selectors
            cp == 0x200D || cp == 0x20E3 // zero-width joiner, keycap

    /** Drops whole lines made only of separator characters (3+ of them). */
    private fun removeSeparatorLines(text: Mapped): Mapped = removeLines(text) { line ->
        val stripped = line.filterNot { it.isWhitespace() }
        stripped.length >= 3 && stripped.all(::isSeparatorChar)
    }

    /** Drops the content of every line matching [drop] (its newline stays; blank runs collapse later). */
    private fun removeLines(text: Mapped, drop: (String) -> Boolean): Mapped {
        val out = Mapped()
        var lineStart = 0
        while (lineStart <= text.length) {
            val nl = text.chars.indexOf("\n", lineStart).let { if (it < 0) text.length else it }
            if (!drop(text.chars.substring(lineStart, nl))) {
                for (i in lineStart until nl) out.add(text.chars[i], text.src[i])
            }
            if (nl < text.length) out.add('\n', text.src[nl])
            lineStart = nl + 1
        }
        return out
    }

    /** ASCII/Arabic rule characters, plus box-drawing and block elements ("═══", "▬▬") used in Facebook posts. */
    internal fun isSeparatorChar(c: Char): Boolean = c in SEPARATOR_CHARS || c in '─'..'▟' || c == '▬'

    /** "..", "...", "....", "……" -> a single "…". */
    private fun normalizeEllipses(text: Mapped): Mapped {
        val out = Mapped()
        var i = 0
        while (i < text.length) {
            val c = text.chars[i]
            if (c == '.' || c == ELLIPSIS) {
                var j = i
                var dots = 0
                while (j < text.length && (text.chars[j] == '.' || text.chars[j] == ELLIPSIS)) {
                    dots += if (text.chars[j] == ELLIPSIS) 3 else 1
                    j++
                }
                if (dots >= 2) out.add(ELLIPSIS, text.src[i]) else out.add(c, text.src[i])
                i = j
            } else {
                out.add(c, text.src[i])
                i++
            }
        }
        return out
    }

    /** Spaces/tabs collapse to one space; any blank-line run collapses to one newline. */
    private fun collapseWhitespace(text: Mapped): Mapped {
        val out = Mapped()
        var i = 0
        while (i < text.length) {
            val c = text.chars[i]
            if (c.isWhitespace()) {
                var j = i
                var sawNewline = false
                while (j < text.length && text.chars[j].isWhitespace()) {
                    if (text.chars[j] == '\n') sawNewline = true
                    j++
                }
                out.add(if (sawNewline) '\n' else ' ', text.src[i])
                i = j
            } else {
                out.add(c, text.src[i])
                i++
            }
        }
        return out
    }

    /** Leading: whitespace + any stray punctuation. Trailing: whitespace + stray
     * punctuation that isn't sentence-final. */
    private fun trimEnds(text: Mapped): Mapped {
        var from = 0
        var to = text.length
        while (from < to && (text.chars[from].isWhitespace() || text.chars[from] in STRAY)) from++
        while (to > from) {
            val c = text.chars[to - 1]
            if (c.isWhitespace() || (c in STRAY && c !in SENTENCE_FINAL)) to-- else break
        }
        return text.slice(from, to)
    }

    private fun removeUnmatchedPairs(text: Mapped): Mapped {
        val drop = HashSet<Int>()
        val stack = ArrayDeque<Int>()
        val closers = PAIRS.entries.associate { (open, close) -> close to open }
        text.chars.forEachIndexed { i, c ->
            when {
                c in PAIRS -> stack.addLast(i)
                c in closers -> {
                    val top = stack.lastOrNull()
                    if (top != null && text.chars[top] == closers[c]) stack.removeLast() else drop += i
                }
            }
        }
        drop += stack
        // Straight double quotes have no direction: an odd count means one is unmatched - drop them all.
        val straight = text.chars.indices.filter { text.chars[it] == '"' }
        if (straight.size % 2 == 1) drop += straight
        return text.filterIndexed { i -> i !in drop }
    }

    /** Text with, for every char, the offset in the full article it came from (-1 = inserted). */
    private class Mapped {
        val chars = StringBuilder()
        val src = ArrayList<Int>()
        val length: Int get() = chars.length

        fun add(c: Char, from: Int) {
            chars.append(c)
            src.add(from)
        }

        fun isEmpty() = chars.isEmpty()
        fun last(): Char = chars[chars.length - 1]
        fun append(c: Char): Mapped = also { add(c, -1) }

        fun slice(from: Int, to: Int): Mapped = Mapped().also { out ->
            for (i in from until to) out.add(chars[i], src[i])
        }

        fun filterIndexed(keep: (Int) -> Boolean): Mapped = Mapped().also { out ->
            for (i in 0 until length) if (keep(i)) out.add(chars[i], src[i])
        }

        override fun toString(): String = chars.toString()

        companion object {
            fun of(text: String, from: Int, to: Int) = Mapped().also { out ->
                for (i in from until to) out.add(text[i], i)
            }
        }
    }
}
