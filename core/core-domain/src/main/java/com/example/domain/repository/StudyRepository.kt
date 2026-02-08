package com.example.domain.repository

import com.example.domain.module.Lesson
import com.example.domain.module.Level
import com.example.domain.module.Playlist
import com.example.domain.module.Student
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface StudyRepository {


    fun getStudentData(): Flow<Student?>

    suspend fun saveStudentData(telegramId: Long)

    suspend fun storeAdminDataToRoom(telegramId: Long)

    suspend fun disconnectTelegram()


    fun getPlaylistsForLevel(levelId: String): Flow<List<Playlist>?>

    suspend fun syncPlaylists()


    suspend fun syncLevels()

    fun getLevels(): Flow<List<Level>>


    fun getLessonsForPlaylist(playlistId: String): Flow<List<Lesson>>


    fun getLessonById(lessonId: String): Flow<Lesson?>


    suspend fun syncLessons()

    suspend fun ensureLessonFilesDownloaded(id: String)




    // admin

    suspend fun getRemotePlaylistForLevel(levelId: String): List<Playlist>
    suspend fun uploadPlaylist(playlist: Playlist) : Flow<UploadResult>

    suspend fun getRemoteLessonsForPlaylist(playlistId: String): List<Lesson>
    suspend fun uploadLesson(lesson: Lesson)

    suspend fun getRemoteLessonById(lessonId: String): Lesson





}