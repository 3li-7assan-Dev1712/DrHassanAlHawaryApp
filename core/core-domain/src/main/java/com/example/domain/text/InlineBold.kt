package com.example.domain.text

/**
 * WhatsApp/Telegram-style `*bold*` markers in article text. The reader shows them as
 * bold spans ([parse]); everywhere else they're plain text ([strip]).
 */
object InlineBold {

    /** The text without markers, plus the bold ranges (end exclusive) in that text. */
    data class Parsed(val text: String, val bold: List<IntRange>)

    fun strip(text: String): String = MARKED.replace(text) { it.groupValues[1] }

    fun parse(text: String): Parsed {
        val out = StringBuilder(text.length)
        val ranges = mutableListOf<IntRange>()
        var last = 0
        MARKED.findAll(text).forEach { match ->
            out.append(text, last, match.range.first)
            val start = out.length
            out.append(match.groupValues[1])
            ranges += start until out.length
            last = match.range.last + 1
        }
        out.append(text, last, text.length)
        return Parsed(out.toString(), ranges)
    }

    // One line, not starting/ending with a space, so "5 * 3 * 2" isn't read as markup.
    private val MARKED = Regex("\\*(?=\\S)([^*\\n]*?\\S)\\*")
}
