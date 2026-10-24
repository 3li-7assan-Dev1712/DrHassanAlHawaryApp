package com.example.domain.module

data class ContentCategory(
    /** What the items store as their `categoryId` ("fatawah"). */
    val id: String,
    val title: String,
    val type: ContentType,
    val description: String? = null,
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    /** Display order among [type]'s categories. */
    val order: Long = 0L,
    /** The category's document in `categories`; null for a built-in one not stored yet. */
    val documentId: String? = null,
)

/** The collection whose items a category of this type groups ("audios"). */
val ContentType.collectionName: String
    get() = when (this) {
        ContentType.AUDIO -> "audios"
        ContentType.VIDEO -> "videos"
        ContentType.ARTICLE -> "articles"
    }

/**
 * The built-in categories: what the apps show until the `categories` collection has the
 * type's own (see CategoryRepository), and what the admin stores there on its first edit.
 */
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
        }.mapIndexed { index, category -> category.copy(order = index + 1L) }
    }
}
