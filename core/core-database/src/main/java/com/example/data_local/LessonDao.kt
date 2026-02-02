package com.example.data_local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_local.model.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lessons: List<LessonEntity>)


    @Query(
        """
        SELECT * FROM lessons 
        WHERE playlistId = :playlistId 
        ORDER BY `order`
    """
    )
    fun getLessonsForPlaylist(playlistId: String): Flow<List<LessonEntity>?>


    @Query("SELECT MAX(updatedAt) FROM lessons")
    suspend fun lastUpdatedAt(): Long?


}