package com.example.domain.repository

import com.example.domain.module.Article
import com.example.domain.module.Audio
import com.example.domain.module.ImageGroup
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.module.Video
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {

    /** The live series of [collection] (only [categoryId]'s when given), in display order. */
    fun getSeries(collection: SeriesCollection, categoryId: String? = null): Flow<List<Series>>

    fun getSeriesById(seriesId: String): Flow<Series?>

    /** Pulls the series changed since the last sync (by updatedAt) into the local cache. */
    suspend fun syncSeries()

    // A series' items, by episodeNumber, from the local cache (see refreshSeriesItems).
    fun getSeriesAudios(seriesId: String): Flow<List<Audio>>

    fun getSeriesArticles(seriesId: String): Flow<List<Article>>

    fun getSeriesVideos(seriesId: String): Flow<List<Video>>

    fun getSeriesImageGroups(seriesId: String): Flow<List<ImageGroup>>

    /**
     * Fetches all of [series]' items from the server into the local cache, where the getters
     * above emit them. False if the fetch failed; what was cached stays.
     */
    suspend fun refreshSeriesItems(series: Series): Boolean
}
