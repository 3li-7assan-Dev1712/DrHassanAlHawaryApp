package com.example.data

import com.example.data.mappers.toDomain
import com.example.data.mappers.toEntity
import com.example.data_local.PlaybackProgressDao
import com.example.domain.module.PlaybackProgress
import com.example.domain.module.SeriesLessonProgress
import com.example.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** The saved positions, in Room only: they belong to this device and are never synced. */
class PlaybackProgressRepositoryImpl @Inject constructor(
    private val dao: PlaybackProgressDao,
) : PlaybackProgressRepository {

    override fun observeAll(): Flow<List<PlaybackProgress>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeSeriesLessons(): Flow<List<SeriesLessonProgress>> =
        dao.observeSeriesLessons().map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(audioUrl: String): PlaybackProgress? = dao.get(audioUrl)?.toDomain()

    override suspend fun save(progress: PlaybackProgress) = dao.upsert(progress.toEntity())
}
