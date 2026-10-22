package com.example.domain.module

/**
 * An ordered course or collection ("شرح صحيح البخاري", "رمضانيات"). Its items live in
 * [collection] and carry its id as `seriesId`, with their place in it as `episodeNumber`.
 */
data class Series(
    val id: String,
    val title: String,
    val collection: SeriesCollection,
    /** The items' category ("scientific_lessons"); null for articles and image groups. */
    val categoryId: String? = null,
    val hashtag: String = "",
    val order: Long = 0L,
    val itemCount: Long = 0L,
    /** Sum of the items' durations; 0 for articles and image groups. */
    val totalDurationMs: Long = 0L,
)

/** The content collections a series can group, with their Firestore collection names. */
enum class SeriesCollection(val firestoreName: String) {
    AUDIOS("audios"),
    ARTICLES("articles"),
    VIDEOS("videos"),
    IMAGE_GROUPS("image_groups");

    companion object {
        fun fromFirestoreName(name: String): SeriesCollection? = entries.find { it.firestoreName == name }
    }
}
