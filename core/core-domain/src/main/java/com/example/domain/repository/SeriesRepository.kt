package com.example.domain.repository

import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {

    /** The live series of [collection] (only [categoryId]'s when given), in display order. */
    fun getSeries(collection: SeriesCollection, categoryId: String? = null): Flow<List<Series>>

    fun getSeriesById(seriesId: String): Flow<Series?>

    /** Pulls the series changed since the last sync (by updatedAt) into the local cache. */
    suspend fun syncSeries()
}
