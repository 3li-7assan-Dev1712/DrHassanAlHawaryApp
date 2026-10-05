package com.example.domain.text

/**
 * Display titles for design (image group) posts. Most are "تصميم - 9 ذو الحجة 1447هـ":
 * the "تصميم - " prefix repeats the section name, so the date becomes the title, with
 * Arabic-Indic digits ("٩ ذو الحجة ١٤٤٧هـ"). Display only; stored titles are untouched.
 */
object DesignTitle {

    fun clean(raw: String): String {
        val trimmed = raw.replace(WHITESPACE, " ").trim()
        val withoutPrefix = PREFIX.replace(trimmed, "").trim()
        return ArabicNumerals.digits(withoutPrefix.ifEmpty { trimmed })
    }

    private val IMAGES = ArabicDates.Unit(singular = "صورة", dual = "صورتان", plural = "صور")

    /** "صورة", "صورتان", "٣ صور", "١١ صورة". */
    fun imageCount(n: Int): String = ArabicDates.count(n.toLong(), IMAGES)

    private val PREFIX = Regex("^تصميم\\s*[-–—:|]\\s*")
    private val WHITESPACE = Regex("\\s+")
}
