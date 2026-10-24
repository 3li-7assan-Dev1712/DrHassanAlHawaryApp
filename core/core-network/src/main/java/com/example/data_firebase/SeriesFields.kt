package com.example.data_firebase

import com.example.domain.module.SeriesPlacement

/**
 * The series fields an upload or an edit writes through the content callables: both of them,
 * as nulls for a standalone item, so moving an item out of its series clears them.
 */
internal fun SeriesPlacement.toFirestoreFields(): Map<String, Any?> = mapOf(
    "seriesId" to seriesId,
    "episodeNumber" to episodeNumber.takeIf { seriesId != null },
)

/** A new item's series fields: none for a standalone one (absent reads as null). */
internal fun SeriesPlacement?.toNewItemFields(): Map<String, Any> {
    val seriesId = this?.seriesId ?: return emptyMap()
    val episodeNumber = this.episodeNumber ?: return mapOf("seriesId" to seriesId)
    return mapOf("seriesId" to seriesId, "episodeNumber" to episodeNumber)
}
