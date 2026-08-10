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
            description = "إجابات الشيخ على أسئلة المستفتين في شتى مجالات الشريعة"
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
        )
    )

    fun getCategories(type: ContentType): List<ContentCategory> {
        return when (type) {
            ContentType.AUDIO -> AUDIO_CATEGORIES
            else -> emptyList()
        }
    }
}
