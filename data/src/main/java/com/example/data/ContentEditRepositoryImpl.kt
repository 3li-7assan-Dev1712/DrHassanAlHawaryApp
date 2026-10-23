package com.example.data

import com.example.data_firebase.ContentFieldsFirestoreSource
import com.example.domain.module.SeriesCollection
import com.example.domain.module.SeriesPlacement
import com.example.domain.repository.ContentEditRepository
import javax.inject.Inject

/** The admin's partial edits; the apps pick them up with their updatedAt delta sync. */
class ContentEditRepositoryImpl @Inject constructor(
    private val source: ContentFieldsFirestoreSource,
) : ContentEditRepository {

    override suspend fun setCategory(collection: SeriesCollection, itemId: String, categoryId: String): Result<Unit> =
        source.setCategory(collection.firestoreName, itemId, categoryId)

    override suspend fun setPlacement(collection: SeriesCollection, itemId: String, placement: SeriesPlacement): Result<Unit> =
        source.setPlacement(collection.firestoreName, itemId, placement)
}
