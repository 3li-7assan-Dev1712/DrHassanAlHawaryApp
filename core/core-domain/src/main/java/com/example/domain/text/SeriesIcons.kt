package com.example.domain.text

/** The icon a series shows on its card and header (core-ui maps each to a Tabler drawable). */
enum class SeriesIcon { Kaaba, MoonStars, Rings, HomeHeart, Hourglass, Scale, Feather, Quote, Book2, Stack2 }

/**
 * A series' icon from its title: the first rule with a keyword the title contains wins, and
 * [SeriesIcon.Stack2] when none does. Title and keywords are compared in
 * [ArabicSearchText.normalize]d form, so tashkeel, tatweel and hamza spellings don't matter
 * ("الفية" finds "ألفية").
 */
object SeriesIcons {

    // Before RULES, which normalizes its keywords as it is built.
    private val WHITESPACE = Regex("\\s+")

    private val RULES: List<Pair<SeriesIcon, List<String>>> = listOf(
        SeriesIcon.Kaaba to listOf("الحج", "المناسك", "مناسك", "العمرة"),
        SeriesIcon.MoonStars to listOf("الصيام", "رمضان", "الاعتكاف"),
        SeriesIcon.Rings to listOf("النكاح", "الزواج", "الطلاق"),
        SeriesIcon.HomeHeart to listOf("الزوجية", "الأسرة"),
        SeriesIcon.Hourglass to listOf("الجنائز"),
        // Before Feather: "منظومة القواعد الفقهية" is about the rules, not the poem.
        SeriesIcon.Scale to listOf("القواعد الفقهية", "أصول الفقه"),
        SeriesIcon.Feather to listOf("ألفية", "منظومة", "نظم"),
        SeriesIcon.Quote to listOf("حديث"),
        SeriesIcon.Book2 to listOf("صحيح", "سنن", "كتاب", "متن"),
    ).map { (icon, keywords) -> icon to keywords.map(::normalized) }

    fun forTitle(title: String): SeriesIcon {
        val text = normalized(title)
        return RULES.firstOrNull { (_, keywords) -> keywords.any(text::contains) }?.first ?: SeriesIcon.Stack2
    }

    private fun normalized(text: String): String = ArabicSearchText.normalize(text).replace(WHITESPACE, " ")
}
