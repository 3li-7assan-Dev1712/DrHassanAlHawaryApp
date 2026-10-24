package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * An ordered course or collection ("شرح صحيح البخاري"): one document in `series`. Its items
 * live in [collection] and point back to it with `seriesId` / `episodeNumber`.
 */
@Keep
@IgnoreExtraProperties
data class SeriesDto(
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    val id: String = "",
    val title: String = "",
    /** "audios", "articles", "videos" or "image_groups". */
    val collection: String = "",
    /** The items' category ("scientific_lessons"); null for articles and image groups. */
    val categoryId: String? = null,
    val hashtag: String = "",
    val order: Long = 0L,
    val itemCount: Long = 0L,
    /** Sum of the items' durations; 0 for articles and image groups. */
    val totalDurationMs: Long = 0L,
    val updatedAt: Timestamp? = null
)
