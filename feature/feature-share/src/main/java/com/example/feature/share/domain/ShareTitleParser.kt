package com.example.feature.share.domain

/** The pieces of a server title, split so each can be laid out on its own. */
data class ParsedShareTitle(
    val title: String,
    /** The "X" of "X بعنوان: ...", or a bare kind word ("محاضرة - <date>"). */
    val kind: String?,
    /** A weekday named as part of the date intro ("- الجمعة:"), if any. */
    val dayName: String?,
    val hijriDate: HijriDate?,
    val gregorianDate: GregorianDate?,
)

/** What the video card actually shows: title + chip + dates. */
data class ShareCardFields(
    val title: String,
    val kindLabel: String?,
    val hijriDate: HijriDate?,
    val gregorianDate: GregorianDate?,
)

/**
 * Splits the server's combined audio titles into title / kind / dates.
 *
 * Real inputs look like:
 *  - `خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م`
 *  - `محاضرة - 6 ربيع الآخر 1448هـ`
 *
 * Drawing those as one paragraph is what broke the bidi layout (unbalanced
 * "(", separators landing on the wrong side), so the card draws each part as
 * its own RTL block instead. Pure Kotlin - no Android types - so it's covered
 * by JVM unit tests. Never throws, and never drops title text: anything it
 * can't recognise stays in [ParsedShareTitle.title].
 */
object ShareTitleParser {

    fun parse(raw: String): ParsedShareTitle {
        val text = raw.replace(TATWEEL, "").replace(WHITESPACE, " ").trim()
        if (text.isEmpty()) return ParsedShareTitle("", null, null, null, null)

        // Digit mapping is char-for-char, so match indices in `western` are valid in `text`.
        val western = ArabicNumerals.toWestern(text)
        val hijriMatch = HIJRI_REGEX.find(western)
        val gregorianMatch = GREGORIAN_REGEXES.asSequence().mapNotNull { it.find(western) }
            .firstOrNull { gregorianFrom(it) != null }

        val hijri = hijriMatch?.let {
            HijriDate(
                day = it.groupValues[1].toInt(),
                monthName = HIJRI_MONTHS.getValue(it.groupValues[2]),
                year = it.groupValues[3].toInt(),
            )
        }
        val gregorian = gregorianMatch?.let { gregorianFrom(it) }

        val dateMatches = listOfNotNull(hijriMatch, gregorianMatch.takeIf { gregorian != null })
        val cut = dateMatches.minOfOrNull { it.range.first } ?: text.length
        val afterDates = dateMatches.maxOfOrNull { it.range.last + 1 } ?: text.length

        var head = text.substring(0, cut)
        var dayName: String? = null
        if (dateMatches.isNotEmpty()) {
            DAY_INTRO_REGEXES.firstNotNullOfOrNull { it.find(head) }?.let { match ->
                dayName = normalizeDay(match.groupValues[1])
                head = head.substring(0, match.range.first)
            }
        }
        head = trimJunk(head)

        var kind: String? = null
        var title = head
        val titled = KIND_TITLED_REGEX.find(head)
        val prefixed = KIND_PREFIX_REGEX.find(head)
        when {
            titled != null -> {
                kind = titled.groupValues[1].trim()
                title = titled.groupValues[2]
            }
            head in KNOWN_KINDS -> {
                kind = head
                title = ""
            }
            prefixed != null -> {
                kind = prefixed.groupValues[1]
                title = prefixed.groupValues[2]
            }
        }

        // Text after the dates is kept (never silently dropped) unless it's only
        // the leftovers of the date itself: separators, "م", a closing bracket.
        val tail = trimJunk(text.substring(afterDates).replace(TRAILING_ERA, ""))
        if (dateMatches.isNotEmpty() && tail.any { it.isLetter() }) {
            title = if (title.isBlank()) tail else "$title $tail"
        }

        return ParsedShareTitle(
            title = trimJunk(stripParentheses(title)),
            kind = kind?.let { trimJunk(it) }?.takeIf { it.isNotEmpty() },
            dayName = dayName,
            hijriDate = hijri,
            gregorianDate = gregorian,
        )
    }

    /**
     * [parse] + the chip label. The chip comes from the parsed kind first (it's
     * the most specific), then from the audio's category id. If the title itself
     * turns out empty ("محاضرة - <date>"), the kind becomes the title and the chip
     * is hidden rather than repeating the same word twice.
     */
    fun toCardFields(raw: String, categoryId: String?): ShareCardFields {
        val parsed = parse(raw)
        val label = kindLabel(parsed, categoryId)
        return if (parsed.title.isEmpty()) {
            ShareCardFields(label ?: "", null, parsed.hijriDate, parsed.gregorianDate)
        } else {
            ShareCardFields(parsed.title, label, parsed.hijriDate, parsed.gregorianDate)
        }
    }

    fun kindLabel(parsed: ParsedShareTitle, categoryId: String?): String? {
        val mentionsEid = parsed.title.contains("عيد")
        val kind = parsed.kind
        if (kind != null) {
            // Only a bare "خطبة" needs qualifying; "محاضرة", "خطبة العيد", ... are already specific.
            if (kind != KHUTBA) return kind
            return when {
                parsed.dayName == FRIDAY -> "$KHUTBA $FRIDAY"
                mentionsEid -> KHUTBA_EID
                categoryId == CATEGORY_KHOTAB -> KHUTBA_FRIDAY
                else -> KHUTBA
            }
        }
        return when (categoryId) {
            CATEGORY_KHOTAB -> if (mentionsEid) KHUTBA_EID else KHUTBA_FRIDAY
            "scientific_lessons" -> "درس علمي"
            "lectures" -> "محاضرة"
            "fatawah" -> "فتوى"
            "telawat" -> "تلاوة"
            else -> null
        }
    }


    private fun gregorianFrom(match: MatchResult): GregorianDate? {
        val g = match.groupValues
        val (year, month, day) = if (g[1].length == 4) {
            Triple(g[1].toInt(), g[2].toInt(), g[3].toInt())
        } else {
            Triple(g[3].toInt(), g[2].toInt(), g[1].toInt())
        }
        if (month !in 1..12 || day !in 1..31 || year !in 1900..2200) return null
        return GregorianDate(year, month, day)
    }

    /** Drops unmatched brackets (the server's "( date" never closes), then one
     * pair wrapping the whole string. Matched inner pairs are kept. */
    private fun stripParentheses(input: String): String {
        val unmatched = HashSet<Int>()
        val open = ArrayDeque<Int>()
        input.forEachIndexed { i, c ->
            when (c) {
                '(' -> open.addLast(i)
                ')' -> if (open.isEmpty()) unmatched += i else open.removeLast()
            }
        }
        unmatched += open
        var result = input.filterIndexed { i, _ -> i !in unmatched }.trim()
        if (result.startsWith('(') && result.endsWith(')') && isSinglePair(result)) {
            result = result.substring(1, result.length - 1).trim()
        }
        return result
    }

    private fun isSinglePair(s: String): Boolean {
        var depth = 0
        s.forEachIndexed { i, c ->
            if (c == '(') depth++
            if (c == ')') depth--
            if (depth == 0 && i < s.length - 1) return false
        }
        return true
    }

    private fun trimJunk(s: String): String = s.trim { it.isWhitespace() || it in JUNK }

    private fun normalizeDay(day: String): String = when (day) {
        "الاحد" -> "الأحد"
        "الإثنين" -> "الاثنين"
        "الاربعاء" -> "الأربعاء"
        else -> day
    }

    private const val TATWEEL = "ـ"
    private val WHITESPACE = Regex("\\s+")
    // Brackets are deliberately absent: stripParentheses owns them (keeps matched pairs).
    private const val JUNK = "-–—:،,؛;.«»\"'"
    private val TRAILING_ERA = Regex("^\\s*م(?![\\p{L}])")

    private const val KHUTBA = "خطبة"
    private const val FRIDAY = "الجمعة"
    private const val KHUTBA_FRIDAY = "خطبة الجمعة"
    private const val KHUTBA_EID = "خطبة العيد"
    private const val CATEGORY_KHOTAB = "khotab"

    private val KNOWN_KINDS = setOf(
        "محاضرة", "درس", "درس علمي", "خطبة", "خطبة الجمعة", "خطبة العيد",
        "فتوى", "فتاوى", "تلاوة", "كلمة", "لقاء", "موعظة", "خاطرة",
    )

    private val KIND_TITLED_REGEX = Regex("^(.{1,24}?)\\s*بعنوان\\s*[:：]?\\s*(.+)$")
    private val KIND_PREFIX_REGEX = Regex(
        "^(" + KNOWN_KINDS.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) } +
            ")\\s*[:：\\-–—]\\s*(.+)$"
    )

    private val DAYS = listOf(
        "الجمعة", "الخميس", "السبت", "الأحد", "الاحد", "الاثنين", "الإثنين",
        "الثلاثاء", "الأربعاء", "الاربعاء",
    ).joinToString("|")

    /** A weekday introducing the date: either after a dash or followed by a
     * colon. Plain "... يوم الجمعة" at the end of a title is NOT stripped. */
    private val DAY_INTRO_REGEXES = listOf(
        Regex("\\s*[-–—]\\s*(?:يوم\\s+)?($DAYS)\\s*[:：]?\\s*[(\\[]?\\s*$"),
        Regex("\\s+(?:يوم\\s+)?($DAYS)\\s*[:：]\\s*[(\\[]?\\s*$"),
    )

    /** Spelling variants seen in titles -> the canonical name the card prints. */
    private val HIJRI_MONTHS: Map<String, String> = buildMap {
        fun add(canonical: String, vararg variants: String) {
            put(canonical, canonical)
            variants.forEach { put(it, canonical) }
        }
        add("محرم", "المحرم")
        add("صفر")
        add("ربيع الأول", "ربيع الاول", "ربيع أول", "ربيع اول")
        add("ربيع الآخر", "ربيع الاخر", "ربيع الثاني", "ربيع ثاني", "ربيع آخر")
        add("جمادى الأولى", "جمادى الاولى", "جمادى الأول", "جمادى الاول", "جمادي الأولى", "جمادي الاولى")
        add("جمادى الآخرة", "جمادى الاخرة", "جمادى الثانية", "جمادى الآخر", "جمادي الآخرة", "جمادي الاخرة")
        add("رجب")
        add("شعبان")
        add("رمضان")
        add("شوال")
        add("ذو القعدة", "ذي القعدة", "ذو القعده", "ذي القعده", "ذى القعدة")
        add("ذو الحجة", "ذي الحجة", "ذو الحجه", "ذي الحجه", "ذى الحجة")
    }

    private val HIJRI_REGEX = Regex(
        "(\\d{1,2})\\s*(" +
            HIJRI_MONTHS.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) } +
            ")\\s*[،,]?\\s*(\\d{3,4})\\s*(?:هـ|ه(?![\\p{L}]))?"
    )

    private val GREGORIAN_REGEXES = listOf(
        Regex("(\\d{4})\\s*[/\\-.]\\s*(\\d{1,2})\\s*[/\\-.]\\s*(\\d{1,2})\\s*(?:م(?![\\p{L}]))?"),
        Regex("(\\d{1,2})\\s*[/\\-.]\\s*(\\d{1,2})\\s*[/\\-.]\\s*(\\d{4})\\s*(?:م(?![\\p{L}]))?"),
    )
}
