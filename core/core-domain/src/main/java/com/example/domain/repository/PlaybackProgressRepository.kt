package com.example.domain.repository

import com.example.domain.module.PlaybackProgress
import com.example.domain.module.SeriesLessonProgress
import kotlinx.coroutines.flow.Flow

/** Where each audio was left, kept on the device only. */
interface PlaybackProgressRepository {

    /** Every saved position, the latest first. */
    fun observeAll(): Flow<List<PlaybackProgress>>

    /** The saved positions of the cached audios that are lessons in a series. */
    fun observeSeriesLessons(): Flow<List<SeriesLessonProgress>>

    suspend fun get(audioUrl: String): PlaybackProgress?

    /** Replaces the audio's saved position. */
    suspend fun save(progress: PlaybackProgress)
}
