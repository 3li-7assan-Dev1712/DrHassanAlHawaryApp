package com.example.domain.text

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil

/**
 * Display cleaning for article text scraped from Facebook posts. Applied in the
 * mapper/repository layer, never in UI code, and never written back: the stored
 * article stays exactly as scraped.
 *
 * - [cleanTitle]: the article title only - no author suffix, no Facebook
 *   timestamp (English or Arabic), first " - " turned into ": ".
 * - [cleanBody]: emojis, tatweel, separator lines, post bylines, a leading
 *   basmala and [boilerplate] lines removed; blank lines collapsed.
 * - [excerpt]: the first meaningful paragraph of the cleaned body - not a repeat
 *   of the title, not a short heading or an "…وبعد:" opening.
 */
class ArticleTextCleaner(
    /** Lines dropped from bodies when they START with any of these (after cleanup). */
    private val boilerplate: List<String> = DEFAULT_BOILERPLATE,
) {

    fun cleanTitle(raw: String): String {
        var title = raw
        ARABIC_TIMESTAMPS.forEach { title = it.replace(title, " ") }
        return ArticleText.cleanTitle(title)
    }

    /**
     * Cleaned paragraphs, one per line, as plain text (no emojis, no `*bold*` markers, no
     * leading basmala). With [rawTitle], header lines repeating the title or a part of it
     * are dropped too.
     */
    fun cleanBody(raw: String, rawTitle: String = ""): String {
        val lines = bodyLines(raw, rawTitle, reader = false)
        // "Leading" basmala = in the opening block, before the prose starts (the first line
        // that ends a sentence). Posts put it after the title/byline, not always on line 1;
        // a basmala quoted later in the article is content and stays.
        val proseStart = lines.indexOfFirst { it.trimEnd().lastOrNull()?.let { c -> c in SENTENCE_END } == true }
            .let { if (it < 0) lines.size else it }
        return lines.filterIndexed { i, line -> !(i < proseStart && isBasmala(line)) }.joinToString("\n")
    }

    /**
     * Paragraphs for the article reader. The same header cleanup as [cleanBody], but the
     * author's emojis, `*bold*` markers (see [InlineBold]) and a basmala are kept, and a
     * decorative separator inside the body becomes one [ORNAMENT] paragraph (never first
     * or last).
     */
    fun readerParagraphs(raw: String, rawTitle: String = ""): List<String> =
        bodyLines(raw, rawTitle, reader = true).dropLastWhile { it == ORNAMENT }

    /** True for a short line that is just the basmala (diacritics allowed). */
    fun isBasmala(line: String): Boolean = BASMALA.containsMatchIn(line.trim()) && line.length <= BASMALA_LINE_MAX

    /**
     * "▪ أولًا: …" -> "أولًا: …" for a line that opens a numbered section (أولًا … عاشرًا
     * followed by a colon); null for any other line.
     */
    fun sectionHeading(line: String): String? {
        val match = SECTION_HEADING.find(line) ?: return null
        return line.substring(match.groups[1]!!.range.first).trim()
    }

    private fun bodyLines(raw: String, rawTitle: String, reader: Boolean): List<String> {
        val title = " " + normalizeForCompare(InlineBold.strip(cleanTitle(rawTitle))) + " "
        val out = mutableListOf<String>()
        var inHeader = true
        for (rawLine in raw.replace("\r\n", "\n").split('\n')) {
            if (isSeparatorLine(rawLine)) {
                if (reader && !inHeader && out.isNotEmpty() && out.last() != ORNAMENT) out += ORNAMENT
                continue
            }
            // A removed emoji can leave "طاحنة ،": no space before punctuation.
            val plain = InlineBold.strip(normalizeSpaces(stripEmojisAndTatweel(rawLine)))
                .replace(SPACE_BEFORE_PUNCTUATION, "$1")
            if (plain.isEmpty()) continue
            if (ArticleText.isFacebookByline(plain) || ArticleText.isAuthorLine(plain)) continue
            if (boilerplate.any { plain.startsWith(it) }) continue
            if (inHeader) {
                val compare = normalizeForCompare(plain)
                if (title.isNotBlank() && compare.isNotEmpty() && title.contains(" $compare ")) continue
                if (!isBasmala(plain)) inHeader = false
            }
            out += if (reader) normalizeSpaces(rawLine.replace("ـ", "")) else plain
        }
        return out
    }

    /** The first meaningful paragraph of the cleaned body, or "" if there is none. */
    fun excerpt(rawBody: String, rawTitle: String = ""): String {
        val titleWords = normalizeForCompare(cleanTitle(rawTitle))
        return cleanBody(rawBody, rawTitle).split('\n').firstOrNull { paragraph ->
            val compare = normalizeForCompare(paragraph)
            val words = paragraph.split(' ').count { it.any(Char::isLetter) }
            val repeatsTitle = compare.isNotEmpty() && titleWords.contains(compare)
            val isHeadingOrOpening = paragraph.trimEnd().endsWith(':') || paragraph.trimEnd().endsWith('：')
            words >= MIN_EXCERPT_WORDS && !repeatsTitle && !isHeadingOrOpening
        }.orEmpty()
    }

    /** Reading time at [WORDS_PER_MINUTE], at least one minute. */
    fun readingMinutes(rawBody: String): Int {
        val words = cleanBody(rawBody).split(WHITESPACE).count { it.any(Char::isLetter) }
        return ceil(words / WORDS_PER_MINUTE.toDouble()).toInt().coerceAtLeast(1)
    }

    /**
     * The post date when it only exists inside the text, e.g. the byline's
     * "September 23 at 9:55 PM" (no year: the most recent such date not after
     * [nowMillis]). Must be read BEFORE cleaning, which strips it. Null if none.
     */
    fun publishedAtInText(raw: String, nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long? {
        val match = ENGLISH_DATE.find(raw) ?: return null
        val month = MONTHS.indexOfFirst { match.groupValues[1].lowercase(Locale.ROOT).startsWith(it) }
        if (month < 0) return null
        val day = match.groupValues[2].toInt()
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val year = match.groupValues[3].takeIf { it.isNotEmpty() }?.toInt() ?: calendar.get(Calendar.YEAR)
        var hour = match.groupValues[4].takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val minute = match.groupValues[5].takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val amPm = match.groupValues[6].lowercase(Locale.ROOT)
        if (amPm == "pm" && hour < 12) hour += 12
        if (amPm == "am" && hour == 12) hour = 0
        calendar.clear()
        calendar.set(year, month, day, hour, minute)
        // No year in the text: a date "in the future" must be last year's.
        if (match.groupValues[3].isEmpty() && calendar.timeInMillis > nowMillis) calendar.add(Calendar.YEAR, -1)
        return calendar.timeInMillis
    }

    private fun stripEmojisAndTatweel(line: String): String = buildString(line.length) {
        var i = 0
        while (i < line.length) {
            val cp = Character.codePointAt(line, i)
            if (!TextSanitizer.isEmojiOrJoiner(cp) && cp != 0x0640) appendCodePoint(cp)
            i += Character.charCount(cp)
        }
    }

    /** 3+ rule characters ("=====", "═══✿✿✿═══", "ـــــ", "•••"), ornaments allowed. */
    private fun isSeparatorLine(line: String): Boolean {
        val stripped = line.filterNot { it.isWhitespace() || it in '︀'..'️' }
        return stripped.length >= 3 && stripped.all { TextSanitizer.isSeparatorChar(it) || it in ORNAMENTS }
    }

    private fun normalizeSpaces(line: String) = line.replace(WHITESPACE, " ").trim()

    /** Letters only, no diacritics/punctuation, for "is this line just the title again?". */
    private fun normalizeForCompare(text: String) =
        text.filter { it.isLetter() || it == ' ' }.replace(WHITESPACE, " ").trim()

    companion object {
        val DEFAULT_BOILERPLATE = listOf("خدمة المقالات والمقتطفات")

        /** What a separator inside the reader body turns into. */
        const val ORNAMENT = "✦ ✦ ✦"
        private const val ORNAMENTS = "✿❀❁✽✾✦✧❖◆◇○●◦▪▫■□"
        private val SECTION_HEADING = Regex(
            "^[▪▫•◦■□\\s]*((?:أول|ثاني|ثالث|رابع|خامس|سادس|سابع|ثامن|تاسع|عاشر)(?:ًا|اً|ا)\\s*[:：])",
        )

        const val WORDS_PER_MINUTE = 180
        private const val MIN_EXCERPT_WORDS = 5
        private const val BASMALA_LINE_MAX = 40
        private const val SENTENCE_END = ".؟?!…"

        private val WHITESPACE = Regex("\\s+")
        private val SPACE_BEFORE_PUNCTUATION = Regex(" ([،,.؛:؟!])")
        // Diacritics may appear between the letters.
        private val BASMALA = Regex("^ب\\p{Mn}*س\\p{Mn}*م\\p{Mn}*\\s+ا\\p{Mn}*ل\\p{Mn}*ل\\p{Mn}*ه")

        /** "٤ د", "3 س", "منذ ساعتين", "منذ ٣ أيام" - Facebook's relative stamps. */
        private val ARABIC_TIMESTAMPS = listOf(
            Regex("\\s*[-–—|·•]?\\s*منذ\\s+\\S+(?:\\s+\\S+)?\\s*$"),
            Regex("\\s*[-–—|·•]?\\s*[0-9٠-٩]+\\s*(?:د|س|ي|ث|أ)\\s*$"),
        )

        private val MONTHS = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
        private val ENGLISH_DATE = Regex(
            "\\b(January|February|March|April|May|June|July|August|September|October|November|December|" +
                "Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\s+(\\d{1,2})(?:,\\s*(\\d{4}))?" +
                "(?:\\s+at\\s+(\\d{1,2}):(\\d{2})\\s*(AM|PM|am|pm)?)?",
        )
    }
}
