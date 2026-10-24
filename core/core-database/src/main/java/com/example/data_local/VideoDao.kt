package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_local.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE isDeleted = 0 ORDER BY publishDate DESC LIMIT 1")
    suspend fun getLastVideo(): VideoEntity?

    /** The paged list shows standalone videos only: a series' videos are reached through its card. */
    @Query(
        """
        SELECT * FROM videos
        WHERE isDeleted = 0
        AND seriesId IS NULL
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY publishDate DESC
    """
    )
    fun pagingSource(categoryId: String?): PagingSource<Int, VideoEntity>

    @Query("SELECT COUNT(*) FROM videos WHERE isDeleted = 0")
    suspend fun count(): Int

    @Query("SELECT * FROM videos WHERE seriesId = :seriesId AND isDeleted = 0 ORDER BY episodeNumber ASC")
    fun getSeriesVideos(seriesId: String): Flow<List<VideoEntity>>

    /** Videos cached as part of [seriesId] that the server no longer lists in it become standalone. */
    @Query("UPDATE videos SET seriesId = NULL, episodeNumber = NULL WHERE seriesId = :seriesId AND id NOT IN (:keepIds)")
    suspend fun leaveSeries(seriesId: String, keepIds: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("DELETE FROM videos")
    suspend fun clearAll()

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteById(videoId: String)

    /** Which of [ids] are cached. */
    @Query("SELECT id FROM videos WHERE id IN (:ids)")
    suspend fun getCachedIds(ids: List<String>): List<String>
}
