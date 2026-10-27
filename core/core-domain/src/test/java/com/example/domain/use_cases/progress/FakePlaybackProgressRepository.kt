package com.example.domain.use_cases.progress

import com.example.domain.module.PlaybackProgress
import com.example.domain.module.SeriesLessonProgress
import com.example.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In memory; [seriesOf] says which audio URLs are lessons of which series. */
class FakePlaybackProgressRepository(
    private val seriesOf: Map<String, String> = emptyMap(),
) : PlaybackProgressRepository {

    private val saved = MutableStateFlow<Map<String, PlaybackProgress>>(emptyMap())

    override fun observeAll(): Flow<List<PlaybackProgress>> =
        saved.map { all -> all.values.sortedByDescending { it.updatedAt } }

    override fun observeSeriesLessons(): Flow<List<SeriesLessonProgress>> =
        saved.map { all -> all.values.mapNotNull { p -> seriesOf[p.audioUrl]?.let { SeriesLessonProgress(it, p) } } }

    override suspend fun get(audioUrl: String): PlaybackProgress? = saved.value[audioUrl]

    override suspend fun save(progress: PlaybackProgress) {
        saved.value = saved.value + (progress.audioUrl to progress)
    }
}
