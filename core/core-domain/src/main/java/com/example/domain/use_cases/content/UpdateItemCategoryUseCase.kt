package com.example.domain.use_cases.content

import com.example.domain.module.SeriesCollection
import com.example.domain.repository.ContentEditRepository
import javax.inject.Inject

/** Moves an audio or video to another category, leaving the rest of it as it is. */
class UpdateItemCategoryUseCase @Inject constructor(
    private val repository: ContentEditRepository
) {
    suspend operator fun invoke(collection: SeriesCollection, itemId: String, categoryId: String): Result<Unit> =
        repository.setCategory(collection, itemId, categoryId)
}
