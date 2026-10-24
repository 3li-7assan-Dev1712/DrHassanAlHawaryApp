package com.example.domain.repository

import com.example.domain.module.SeriesCollection
import com.example.domain.module.SeriesPlacement

/** Admin edits of one field group of an item, without touching the rest of it. */
interface ContentEditRepository {

    /** Moves an audio or video to category [categoryId]. */
    suspend fun setCategory(collection: SeriesCollection, itemId: String, categoryId: String): Result<Unit>

    /** Puts an item in a series at an episode, or makes it standalone. */
    suspend fun setPlacement(collection: SeriesCollection, itemId: String, placement: SeriesPlacement): Result<Unit>
}
