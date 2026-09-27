package com.example.domain.text

import java.text.BreakIterator
import java.util.Locale

/**
 * Sentence-based text selection for the "share as image" flow. The screen shows the
 * article exactly as the reader cleans it ([ArticleTextCleaner.readerLines]) and lets
 * the user tap whole sentences; every sentence keeps its range in
 * [ArticleText.displayText], which is what the quote-image step receives (it rebuilds
 * that same text from the article and sanitizes the range itself).
 */

/** One tappable sentence: [start]..[end] (end exclusive) in the display text, and what to show. */
data class QuoteSentence(val index: Int, val start: Int, val end: Int, val text: String)

/** A block of the selectable article. */
sealed interface QuoteBlock {
    data class Paragraph(val kind: Kind, val sentences: List<QuoteSentence>) : QuoteBlock
    /** A decorative separator inside the body: shown, never selectable. */
    data object Ornament : QuoteBlock

    enum class Kind { Body, Heading, Basmala }
}

class QuoteDocument(
    /** [ArticleText.displayText] of the article: the text every sentence range points into. */
    val displayText: String,
    val blocks: List<QuoteBlock>,
) {
    val sentences: List<QuoteSentence> = blocks.flatMap { (it as? QuoteBlock.Paragraph)?.sentences.orEmpty() }

    /** The display-text range covered by sentences [first]..[last] (paragraph breaks included). */
    fun range(first: Int, last: Int): IntRange = sentences[first].start until sentences[last].end

    companion object {
        private val cleaner = ArticleTextCleaner()
        private const val PARAGRAPH_BREAK = "\n\n"

        fun build(content: String, rawTitle: String): QuoteDocument {
            val displayText = ArticleText.displayText(content)
            // displayText is exactly the trimmed non-blank lines of content joined by
            // "\n\n" (see ArticleText.displayText), so each raw line's offset is known.
            val rawLines = content.split("\n")
            val lineStart = HashMap<Int, Int>()
            var offset = 0
            rawLines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    lineStart[index] = offset
                    offset += trimmed.length + PARAGRAPH_BREAK.length
                }
            }

            val blocks = mutableListOf<QuoteBlock>()
            var sentenceIndex = 0
            var firstBody = true
            for ((lineIndex, text) in cleaner.readerLines(content, rawTitle)) {
                if (lineIndex < 0) {
                    blocks += QuoteBlock.Ornament
                    continue
                }
                val start = lineStart[lineIndex] ?: continue
                val raw = rawLines[lineIndex].trim()
                val kind = when {
                    firstBody && cleaner.isBasmala(text) -> QuoteBlock.Kind.Basmala
                    cleaner.sectionHeading(text) != null -> QuoteBlock.Kind.Heading
                    else -> QuoteBlock.Kind.Body
                }
                firstBody = false
                val ranges = if (kind == QuoteBlock.Kind.Body) SentenceSplitter.split(raw) else listOf(0 until raw.length)
                val sentences = ranges.map { r ->
                    QuoteSentence(sentenceIndex++, start + r.first, start + r.last + 1, displayForm(raw.substring(r.first, r.last + 1), kind))
                }
                if (sentences.isNotEmpty()) blocks += QuoteBlock.Paragraph(kind, sentences)
            }
            return QuoteDocument(displayText, blocks)
        }

        /** What the screen shows for a sentence: no tatweel, no `*bold*` markers, single spaces. */
        private fun displayForm(raw: String, kind: QuoteBlock.Kind): String {
            val text = InlineBold.strip(raw.replace("ـ", "")).replace(Regex("\\s+"), " ").trim()
            return if (kind == QuoteBlock.Kind.Heading) cleaner.sectionHeading(text) ?: text else text
        }
    }
}

/**
 * Splits one paragraph into sentences with the platform's Arabic sentence rules. A leading
 * list number ("١." / "1.") or any piece shorter than [MIN_SENTENCE] characters is merged
 * into the sentence after it (or before it, at the paragraph's end). Never splits on "،".
 * Returns ranges (inclusive) into [paragraph], trimmed of surrounding spaces.
 */
object SentenceSplitter {
    const val MIN_SENTENCE = 12
    private val LIST_NUMBER = Regex("^[0-9٠-٩]{1,3}\\s*[.)\\-–]$")
    private val TRAILING_LIST_NUMBER = Regex("\\s[0-9٠-٩]{1,3}\\s*[.)\\-–]$")

    fun split(paragraph: String): List<IntRange> {
        if (paragraph.isBlank()) return emptyList()
        val pieces = mutableListOf<IntRange>()
        val iterator = BreakIterator.getSentenceInstance(Locale("ar")).apply { setText(paragraph) }
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            splitOnQuestionMarks(paragraph, start, end).forEach { pieces += trimmed(paragraph, it) ?: return@forEach }
            start = end
            end = iterator.next()
        }

        // The JDK rules glue the next item's number onto the previous sentence
        // ("…الأخرى. ٢." | "غلاء…"): cut a trailing list number off into its own piece.
        for (i in pieces.lastIndex - 1 downTo 0) {
            val piece = pieces[i]
            val match = TRAILING_LIST_NUMBER.find(paragraph.substring(piece.first, piece.last + 1)) ?: continue
            val cut = piece.first + match.range.first
            trimmed(paragraph, piece.first until cut)?.let { head ->
                pieces[i] = head
                pieces.add(i + 1, trimmed(paragraph, cut..piece.last) ?: return@let)
            }
        }

        // Merge list numbers and short fragments forward; a short LAST piece merges backward.
        val merged = mutableListOf<IntRange>()
        var pendingStart: Int? = null
        pieces.forEachIndexed { i, piece ->
            val text = paragraph.substring(piece.first, piece.last + 1)
            val isFragment = LIST_NUMBER.matches(text) || text.length < MIN_SENTENCE
            val from = pendingStart ?: piece.first
            when {
                isFragment && i < pieces.lastIndex -> {
                    pendingStart = from
                    return@forEachIndexed
                }
                isFragment && pendingStart == null && merged.isNotEmpty() ->
                    merged[merged.lastIndex] = merged.last().first..piece.last
                else -> merged += from..piece.last
            }
            pendingStart = null
        }
        return merged
    }

    /** The JDK's rules don't always end a sentence at the Arabic "؟": split there too. */
    private fun splitOnQuestionMarks(text: String, start: Int, end: Int): List<IntRange> {
        val out = mutableListOf<IntRange>()
        var from = start
        for (i in start until end) {
            if (text[i] == '؟' && i + 1 < end && text[i + 1].isWhitespace()) {
                out += from..i
                from = i + 1
            }
        }
        out += from until end
        return out
    }

    private fun trimmed(text: String, range: IntRange): IntRange? {
        var a = range.first
        var b = range.last
        while (a <= b && text[a].isWhitespace()) a++
        while (b >= a && text[b].isWhitespace()) b--
        return if (a > b) null else a..b
    }
}

/**
 * The contiguous run of selected sentences, [first]..[last] (inclusive), or none.
 * Tapping: nothing selected → that sentence; the sentence just before/after → extend;
 * an end of the selection → remove it; anything else → a new single-sentence selection.
 */
data class SentenceSelection(val first: Int, val last: Int) {
    init {
        require(first <= last) { "selection must be contiguous: $first > $last" }
    }

    companion object {
        fun tap(current: SentenceSelection?, index: Int): SentenceSelection? {
            if (current == null) return SentenceSelection(index, index)
            val (first, last) = current
            return when {
                index == first - 1 -> SentenceSelection(index, last)
                index == last + 1 -> SentenceSelection(first, index)
                first == last && index == first -> null
                index == first -> SentenceSelection(first + 1, last)
                index == last -> SentenceSelection(first, last - 1)
                else -> SentenceSelection(index, index)
            }
        }
    }
}

/** "٩٨ حرفًا · صورة واحدة" */
fun quoteCounter(characters: Int, images: Int): String =
    ArabicDates.count(characters.toLong(), ArabicDates.CHARACTER) + ArabicNumerals.DATE_SEPARATOR +
        ArabicDates.count(images.toLong(), ArabicDates.IMAGE)
