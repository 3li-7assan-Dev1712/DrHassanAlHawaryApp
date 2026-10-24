package com.example.domain.text

/**
 * Display cleaning for audio (fatwa) titles copied from the Telegram service posts:
 * "خدمة المقاطع الصوتية - مقطع بعنوان: حكم لبس النقاب - خدمة فضيلة الشيخ د. حسن"
 * -> "حكم لبس النقاب". Display only; the stored title is never changed. If nothing is
 * left after cleaning, the original is returned.
 */
object AudioTitleCleaner {

    fun clean(raw: String): String {
        var title = buildString(raw.length) {
            var i = 0
            while (i < raw.length) {
                val cp = Character.codePointAt(raw, i)
                if (!TextSanitizer.isEmojiOrJoiner(cp) && cp != 0x0640) appendCodePoint(cp)
                i += Character.charCount(cp)
            }
        }
        title = SERVICE_PREFIX.replace(title, "")
        title = TITLED.replace(title, "")
        title = SERVICE_SUFFIX.replace(title, "")
        title = title.replace(WHITESPACE, " ").trim { it.isWhitespace() || it in EDGE_JUNK }
        return title.ifEmpty { raw.trim() }
    }

    /**
     * A lesson's title on its series' screen: [clean], then without the series' name at the
     * start ("شرح صحيح البخاري - ", "شرح صحيح البخاري، ") and the lesson's numbering ("(٢)",
     * "(2)", "٢ -", "- 1"), in either order; brackets left unbalanced are dropped and Western
     * digits become Arabic-Indic. If nothing is left, [clean]'s title.
     */
    fun lesson(raw: String, seriesTitle: String): String {
        val original = clean(raw)
        val seriesName = seriesNamePrefix(seriesTitle)
        var title = original
        while (true) {
            val next = LESSON_NUMBER.replace(seriesName?.replace(title, "") ?: title, "")
            if (next == title) break
            title = next
        }
        title = dropUnbalancedBrackets(title).replace(WHITESPACE, " ").trim { it.isWhitespace() || it in EDGE_JUNK }
        return if (title.isEmpty()) original else ArabicNumerals.digits(title)
    }

    /** [seriesTitle] at the start of a title, any spacing, with the separators after it. */
    private fun seriesNamePrefix(seriesTitle: String): Regex? {
        val words = seriesTitle.trim().split(WHITESPACE).filter { it.isNotEmpty() }
        if (words.isEmpty()) return null
        // Only a whole name: "شرح الحج" is not the start of "شرح الحجاب".
        return Regex("^\\s*" + words.joinToString("\\s+") { Regex.escape(it) } + "(?=$|[\\s$SEPARATORS()\\[\\]0-9٠-٩])[\\s$SEPARATORS]*")
    }

    /** Drops each bracket that has no partner: "(ترجمة المصنف" -> "ترجمة المصنف". */
    private fun dropUnbalancedBrackets(text: String): String {
        val drop = BooleanArray(text.length)
        val open = ArrayDeque<Int>()
        text.forEachIndexed { i, c ->
            when (c) {
                '(', '[' -> open.addLast(i)
                ')', ']' -> {
                    val partner = if (c == ')') '(' else '['
                    if (open.isNotEmpty() && text[open.last()] == partner) open.removeLast() else drop[i] = true
                }
            }
        }
        open.forEach { drop[it] = true }
        return text.filterIndexed { i, _ -> !drop[i] }
    }

    private const val SEPARATORS = "\\-–—،.,:："
    // "(٢) ", "(2) ", "٢ - ", "- 1 " at the start; digits run into a word ("٢٠٢٤م") stay.
    private val LESSON_NUMBER =
        Regex("^[\\s$SEPARATORS]*(?:\\(\\s*[0-9٠-٩]+\\s*\\)|[0-9٠-٩]+)(?=$|[\\s$SEPARATORS()\\[\\]])[\\s$SEPARATORS]*")

    private val SERVICE_PREFIX = Regex("^\\s*خدمة\\s+المقاطع\\s+الصوتية\\s*[-–—:：]?\\s*")
    private val TITLED = Regex("مقطع\\s+بعنوان\\s*[:：]?\\s*")
    private val SERVICE_SUFFIX = Regex("\\s*[-–—]\\s*خدمة\\s+فضيلة\\s+الشيخ.*$")
    private val WHITESPACE = Regex("\\s+")
    private const val EDGE_JUNK = "-–—:：|،,"
}
