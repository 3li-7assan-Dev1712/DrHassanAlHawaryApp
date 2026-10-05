package com.example.domain.module

data class ContentCategory(
    val id: String,
    val title: String,
    val type: ContentType,
    val description: String? = null,
    val imageRes: Int? = null,
    val imageUrl: String? = null
)

object FixedCategories {
    val AUDIO_CATEGORIES = listOf(
        ContentCategory(
            id = "fatawah",
            title = "فتاوى",
            type = ContentType.AUDIO,
            description = "إجابات الشيخ عن أسئلة المستفتين في شتى مجالات الشريعة"
        ),
        ContentCategory(
            id = "scientific_lessons",
            title = "دروس علمية",
            type = ContentType.AUDIO,
            description = "سلاسل علمية تأصيلية في العقيدة والفقه والتفسير والحديث"
        ),
        ContentCategory(
            id = "khotab",
            title = "خطب الجمعة والعيدين",
            type = ContentType.AUDIO,
            description = "خطب الجمعة المنبرية ومواعظ العيدين الفطر والأضحى"
        ),
        ContentCategory(
            id = "lectures",
            title = "محاضرات",
            type = ContentType.AUDIO,
            description = "محاضرات عامة ولقاءات إيمانية متنوعة"
        ), ContentCategory(
            id = "telawat",
            title = "تلاوات",
            type = ContentType.AUDIO,
            description = "تلاوات الشيخ حسن الهوراي"
        )
    )

    val VIDEO_CATEGORIES = listOf(
        ContentCategory(
            id = "fatawah",
            title = "فتاوى",
            type = ContentType.VIDEO,
            description = "إجابات الشيخ عن أسئلة المستفتين في شتى مجالات الشريعة"
        ),
        ContentCategory(
            id = "scientific_lessons",
            title = "دروس علمية",
            type = ContentType.VIDEO,
            description = "سلاسل علمية تأصيلية في العقيدة والفقه والتفسير والحديث"
        ),
        ContentCategory(
            id = "khotab",
            title = "خطب الجمعة والعيدين",
            type = ContentType.VIDEO,
            description = "خطب الجمعة المنبرية ومواعظ العيدين الفطر والأضحى"
        ),
        ContentCategory(
            id = "lectures",
            title = "محاضرات",
            type = ContentType.VIDEO,
            description = "محاضرات عامة ولقاءات إيمانية متنوعة"
        ),
        ContentCategory(
            id = "telawat",
            title = "تلاوات",
            type = ContentType.VIDEO,
            description = "تلاوات قرآنية مختارة بصوت الشيخ"
        )
    )

    fun getCategories(type: ContentType): List<ContentCategory> {
        return when (type) {
            ContentType.AUDIO -> AUDIO_CATEGORIES
            ContentType.VIDEO -> VIDEO_CATEGORIES
            else -> emptyList()
        }
    }
}
