package com.example.domain.use_cases.content

import com.example.domain.module.SeriesCollection
import com.example.domain.module.SeriesPlacement
import com.example.domain.repository.ContentEditRepository
import javax.inject.Inject

/** Puts an item in a series at an episode, or takes it out (standalone). */
class UpdateItemPlacementUseCase @Inject constructor(
    private val repository: ContentEditRepository
) {
    suspend operator fun invoke(collection: SeriesCollection, itemId: String, placement: SeriesPlacement): Result<Unit> =
        repository.setPlacement(collection, itemId, placement)
}
