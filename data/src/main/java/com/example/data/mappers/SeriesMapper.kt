package com.example.data.mappers

import com.example.data_firebase.model.SeriesDto
import com.example.data_local.model.SeriesEntity
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection

fun SeriesDto.toEntity(): SeriesEntity =
    SeriesEntity(
        id = id,
        title = title,
        collection = collection,
        categoryId = categoryId,
        hashtag = hashtag,
        order = order,
        itemCount = itemCount,
        totalDurationMs = totalDurationMs,
        updatedAt = updatedAt?.toDate()?.time ?: 0L,
        isDeleted = isDeleted
    )

/** Null for a collection this app version doesn't know. */
fun SeriesEntity.toDomainModel(): Series? {
    val seriesCollection = SeriesCollection.fromFirestoreName(collection) ?: return null
    return Series(
        id = id,
        title = title,
        collection = seriesCollection,
        categoryId = categoryId,
        hashtag = hashtag,
        order = order,
        itemCount = itemCount,
        totalDurationMs = totalDurationMs
    )
}
