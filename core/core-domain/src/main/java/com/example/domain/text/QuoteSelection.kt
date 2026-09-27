package com.example.domain.text

/**
 * The article as the quote-selection screen shows it: cleaned exactly like the reader
 * ([ArticleTextCleaner.readerLines]) and laid out as ONE continuous [text], so a
 * long-press-and-drag selection can run across paragraphs. Every displayed character
 * remembers where it came from in [displayText] ([ArticleText.displayText]) - the text
 * the quote-image step rebuilds from the article - so a free selection on the cleaned
 * view converts back to the range that step expects ([toSource]).
 */
class SelectableArticle private constructor(
    /** [ArticleText.displayText] of the article: what [toSource] ranges point into. */
    val displayText: String,
    /** What the screen shows. Blocks are separated by [BLOCK_BREAK]. */
    val text: String,
    /** For each char of [text], its offset in [displayText]; -1 for inserted chars. */
    private val sources: IntArray,
    val blocks: List<Block>,
) {
    enum class Kind { Body, Heading, Basmala, Ornament }

    /** A paragraph of [text]: [start]..[end] (end exclusive). */
    data class Block(val kind: Kind, val start: Int, val end: Int)

    /**
     * The [displayText] range (end exclusive) covering the displayed selection
     * [start]..[end], or null if it holds no real text (only breaks or ornaments).
     * Paragraph breaks between selected paragraphs are included.
     */
    fun toSource(start: Int, end: Int): IntRange? {
        val from = start.coerceIn(0, text.length)
        val to = end.coerceIn(from, text.length)
        val first = (from until to).firstOrNull { sources[it] >= 0 } ?: return null
        val last = (to - 1 downTo from).first { sources[it] >= 0 }
        return sources[first] until sources[last] + 1
    }

    companion object {
        /** A single line break: no blank line (and no big gap) between paragraphs. */
        const val BLOCK_BREAK = "\n"
        private val cleaner = ArticleTextCleaner()
        private const val BULLETS = "▪▫•◦■□"

        fun build(content: String, rawTitle: String): SelectableArticle {
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
                    offset += trimmed.length + 2
                }
            }

            val out = StringBuilder()
            val sources = ArrayList<Int>()
            val blocks = mutableListOf<Block>()
            var firstBody = true
            for ((lineIndex, cleaned) in cleaner.readerLines(content, rawTitle)) {
                if (blocks.isNotEmpty()) {
                    out.append(BLOCK_BREAK)
                    repeat(BLOCK_BREAK.length) { sources += -1 }
                }
                val blockStart = out.length
                if (lineIndex < 0) {
                    out.append(ArticleTextCleaner.ORNAMENT)
                    repeat(ArticleTextCleaner.ORNAMENT.length) { sources += -1 }
                    blocks += Block(Kind.Ornament, blockStart, out.length)
                    continue
                }
                val start = lineStart[lineIndex] ?: continue
                val kind = when {
                    firstBody && cleaner.isBasmala(cleaned) -> Kind.Basmala
                    cleaner.sectionHeading(cleaned) != null -> Kind.Heading
                    else -> Kind.Body
                }
                firstBody = false
                appendDisplay(rawLines[lineIndex].trim(), start, kind == Kind.Heading, out, sources)
                blocks += Block(kind, blockStart, out.length)
            }
            return SelectableArticle(displayText, out.toString(), sources.toIntArray(), blocks)
        }

        /**
         * [raw] as shown: no tatweel, no `*bold*` asterisks, single spaces, and for a
         * heading no leading bullet glyph - each kept char mapped to [start] + its index.
         */
        private fun appendDisplay(raw: String, start: Int, heading: Boolean, out: StringBuilder, sources: MutableList<Int>) {
            var i = 0
            if (heading) while (i < raw.length && (raw[i] in BULLETS || raw[i].isWhitespace())) i++
            var pendingSpace = false
            val blockStart = out.length
            while (i < raw.length) {
                val c = raw[i]
                when {
                    c == 'ـ' || c == '*' -> Unit
                    c.isWhitespace() -> pendingSpace = true
                    else -> {
                        if (pendingSpace && out.length > blockStart) {
                            out.append(' ')
                            sources += start + i - 1
                        }
                        pendingSpace = false
                        out.append(c)
                        sources += start + i
                    }
                }
                i++
            }
        }
    }
}

/** "٩٨ حرفًا · صورة واحدة" */
fun quoteCounter(characters: Int, images: Int): String =
    ArabicDates.count(characters.toLong(), ArabicDates.CHARACTER) + ArabicNumerals.DATE_SEPARATOR +
        ArabicDates.count(images.toLong(), ArabicDates.IMAGE)
