package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_local.model.VideoEntity

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY id DESC LIMIT 1")
    suspend fun getLastVideo(): VideoEntity?



    @Query("SELECT * FROM videos ORDER BY publishDate DESC")
    fun pagingSource(): PagingSource<Int, VideoEntity>



    @Query("SELECT COUNT(*) FROM videos")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("DELETE FROM videos")
    suspend fun clearAll()
}