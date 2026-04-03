package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Upsert
    suspend fun upsertAll(lessons: List<LessonEntity>)

    @Query(
        """
        SELECT * FROM lessons 
        WHERE playlistId = :playlistId 
        ORDER BY `order`
    """
    )
    fun getLessonsForPlaylist(playlistId: String): Flow<List<LessonEntity>?>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: String): Flow<LessonEntity?>

    @Query("SELECT MAX(updatedAt) FROM lessons")
    suspend fun lastUpdatedAt(): Long?

    @Query("UPDATE lessons SET audioFilePath = :audio, pdfFilePath = :pdf WHERE id = :id")
    suspend fun updateLessonFiles(id: String, audio: String?, pdf: String?)

    @Query("DELETE FROM lessons WHERE id = :id")
    fun deleteLessonById(id: String)
}
