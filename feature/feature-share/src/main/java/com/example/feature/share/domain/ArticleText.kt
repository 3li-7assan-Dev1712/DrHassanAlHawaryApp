package com.example.feature.share.domain

/**
 * Article text shared between the selection screen (feature-article) and the
 * quote-image share screen. Selection offsets travel between the two screens,
 * so both MUST build the display text with [displayText] - the same function -
 * or the offsets would point at different characters.
 */
object ArticleText {

    /** The article body as shown for selection: trimmed paragraphs, blank lines dropped. */
    fun displayText(content: String): String = content
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

    /**
     * The article title only, for the "من مقال" card: no author suffix (the
     * author is already in the header/footer), no Facebook timestamp, and the
     * first " - " turned into ": ".
     *
     * `الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير - الشيخ د.حسن الهواري`
     * -> `الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير`
     */
    fun cleanTitle(raw: String): String {
        var title = raw.replace("ـ", "").replace(WHITESPACE, " ").trim()
        FACEBOOK_TIMESTAMPS.forEach { title = it.replace(title, " ") }
        title = title.replace(WHITESPACE, " ").trim()
        AUTHOR_SUFFIXES.forEach { title = it.replace(title, "") }
        title = title.trim { it.isWhitespace() || it in TRAILING_JUNK }
        title = FIRST_DASH.replaceFirst(title, ": ")
        return title
    }

    private val WHITESPACE = Regex("\\s+")
    private const val TRAILING_JUNK = "-–—|،,:؛.…"

    private const val MONTHS =
        "January|February|March|April|May|June|July|August|September|October|November|December|" +
            "Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec"
    internal val FACEBOOK_TIMESTAMPS = listOf(
        // "September 23 at 9:55 PM", "Sep 23, 2025 at 9:55 PM", "September 23"
        Regex("\\b(?:$MONTHS)\\s+\\d{1,2}(?:,\\s*\\d{4})?(?:\\s+at\\s+\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm)?)?", RegexOption.IGNORE_CASE),
        // "Yesterday at 10:02 AM", "Today at 8:00"
        Regex("\\b(?:Yesterday|Today)\\s+at\\s+\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm)?", RegexOption.IGNORE_CASE),
    )

    /** " - الشيخ د.حسن الهواري", " - الشيخ د. حسن أحمد الهواري", " - د . حسن الهوارى", "- الشيخ د.حسن ..." */
    private val AUTHOR_SUFFIXES = listOf(
        // No \b: Java's word boundary is ASCII-only and never matches next to Arabic letters.
        Regex("\\s*[-–—|،,]\\s*(?:ال)?شيخ\\s.*$"),
        Regex("\\s*[-–—|،,]\\s*(?:د\\s*\\.\\s*)?حسن\\s+(?:(?:أ|ا)حمد\\s+)?(?:ال)?هوار[يى].*$"),
    )

    private val FIRST_DASH = Regex("\\s+[-–—]\\s+")

    /** "الشيخ د.حسن أحمد الهواري" and its spelling variants, as a whole string. */
    private val AUTHOR_NAME = Regex("^(?:(?:ال)?شيخ\\s*)?(?:د\\s*\\.?\\s*)?حسن\\s+(?:(?:أ|ا)حمد\\s+)?(?:ال)?هوار[يى]$")

    /**
     * True for a Facebook post byline copied into the article body, e.g.
     * "الشيخ د.حسن أحمد الهواري • September 23 at 9:55 PM": it has a timestamp, and
     * besides that only the author's name and "•" separators.
     */
    internal fun isFacebookByline(line: String): Boolean {
        var rest = line.replace("\u0640", "")
        var hadTimestamp = false
        FACEBOOK_TIMESTAMPS.forEach { pattern ->
            if (pattern.containsMatchIn(rest)) hadTimestamp = true
            rest = pattern.replace(rest, " ")
        }
        if (!hadTimestamp) return false
        rest = rest.replace(Regex("[•·|\\-–—]"), " ").replace(WHITESPACE, " ").trim()
        return rest.isEmpty() || AUTHOR_NAME.matches(rest)
    }
}
