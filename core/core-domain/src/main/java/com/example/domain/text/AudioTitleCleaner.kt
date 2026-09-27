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

    private val SERVICE_PREFIX = Regex("^\\s*خدمة\\s+المقاطع\\s+الصوتية\\s*[-–—:：]?\\s*")
    private val TITLED = Regex("مقطع\\s+بعنوان\\s*[:：]?\\s*")
    private val SERVICE_SUFFIX = Regex("\\s*[-–—]\\s*خدمة\\s+فضيلة\\s+الشيخ.*$")
    private val WHITESPACE = Regex("\\s+")
    private const val EDGE_JUNK = "-–—:：|،,"
}
