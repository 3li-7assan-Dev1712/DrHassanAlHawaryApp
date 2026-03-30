package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_local.model.VideoEntity

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE isDeleted = 0 ORDER BY publishDate DESC LIMIT 1")
    suspend fun getLastVideo(): VideoEntity?

    @Query("SELECT * FROM videos WHERE isDeleted = 0 ORDER BY publishDate DESC")
    fun pagingSource(): PagingSource<Int, VideoEntity>

    @Query("SELECT COUNT(*) FROM videos WHERE isDeleted = 0")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("DELETE FROM videos")
    suspend fun clearAll()

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteById(videoId: String)
}
