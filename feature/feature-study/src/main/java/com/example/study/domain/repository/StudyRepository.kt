package com.example.study.domain.repository

import com.example.domain.module.Lesson
import com.example.domain.module.Level
import com.example.domain.module.Playlist
import com.example.study.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudyRepository {


    fun getStudentData(): Flow<Student?>

    suspend fun saveStudentData(telegramId: Long)

    suspend fun disconnectTelegram()


    fun getPlaylistsForLevel(levelId: String): Flow<List<Playlist>?>

    suspend fun syncPlaylists()


    suspend fun syncLevels()

    fun getLevels(): Flow<List<Level>>


    fun getLessonsForPlaylist(playlistId: String): Flow<List<Lesson>>


    fun getLessonById(lessonId: String): Flow<Lesson?>


    suspend fun syncLessons()
}