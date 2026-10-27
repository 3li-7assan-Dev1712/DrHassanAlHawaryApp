package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.PlaybackProgressEntity
import com.example.data_local.model.SeriesLessonProgressRow
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackProgressDao {

    @Query("SELECT * FROM playback_progress ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PlaybackProgressEntity>>

    @Query(
        """
        SELECT audios.seriesId AS seriesId, playback_progress.*
        FROM playback_progress
        INNER JOIN audios ON audios.audioUrl = playback_progress.audioUrl
        WHERE audios.seriesId IS NOT NULL AND audios.isDeleted = 0
        """
    )
    fun observeSeriesLessons(): Flow<List<SeriesLessonProgressRow>>

    @Query("SELECT * FROM playback_progress WHERE audioUrl = :audioUrl")
    suspend fun get(audioUrl: String): PlaybackProgressEntity?

    @Upsert
    suspend fun upsert(progress: PlaybackProgressEntity)
}
