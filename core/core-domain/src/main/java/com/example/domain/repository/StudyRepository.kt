package com.example.domain.repository

import com.example.domain.module.LeaderBoard
import com.example.domain.module.Lesson
import com.example.domain.module.Level
import com.example.domain.module.Playlist
import com.example.domain.module.Quiz
import com.example.domain.module.QuizSubmissionResult
import com.example.domain.module.Student
import com.example.domain.module.UserData
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface StudyRepository {


    fun getStudentData(): Flow<Student?>

    suspend fun getStudentAuthData(): UserData?

    suspend fun saveStudentData(uid: String)

    suspend fun storeAdminDataToRoom(telegramId: Long)

    suspend fun deleteStudentData()

    suspend fun disconnectTelegram()

    suspend fun checkMembership(uid: String, telegramId: Long): Result<Unit>


    fun getPlaylistsForLevel(levelId: String): Flow<List<Playlist>?>

    suspend fun syncPlaylists()

    suspend fun getRemoteMotivationalMessages(): List<String>

    suspend fun syncLevels()

    fun getLevels(): Flow<List<Level>>


    fun getLessonsForPlaylist(playlistId: String): Flow<List<Lesson>>


    fun getLessonById(lessonId: String): Flow<Lesson?>


    suspend fun syncLessons()

    suspend fun ensureLessonFilesDownloaded(id: String)


    suspend fun updatePlaylist(
        playlistId: String,
        newTitle: String,
        newLevelId: String,
        newOrder: Int,
        newThumbnailLocalOrRemote: String?
    ): Result<String>


    // admin

    suspend fun getRemotePlaylistForLevel(levelId: String): List<Playlist>
    suspend fun uploadPlaylist(playlist: Playlist): Flow<UploadResult>
    suspend fun deletePlaylist(playlistId: String): Result<Unit>

    suspend fun getRemoteLessonsForPlaylist(playlistId: String): List<Lesson>


    suspend fun updateLesson(
        lesson: Lesson,
        localAudioUrl: String?,
        localPdfUrl: String?
    ): Result<String>

    suspend fun addLesson(lesson: Lesson, playlistId: String): Flow<UploadResult>
    suspend fun deleteLesson(lessonId: String): Result<Unit>

    suspend fun getRemoteLessonById(lessonId: String): Lesson?
    suspend fun getRemotePlaylistById(playlistId: String): Playlist?

    // Quiz and Leaderboard
    suspend fun uploadQuiz(quiz: Quiz): Result<Unit>
    suspend fun getLatestQuiz(batchId: String): Quiz?
    suspend fun getQuizWithQuestions(batchId: String): Quiz?
    fun observeQuizWithQuestions(batchId: String): Flow<Quiz?>
    suspend fun getAllQuizzes(): List<Quiz>
    suspend fun updateQuizControls(quizId: String, isActive: Boolean, startAt: Long?, endAt: Long?): Result<Unit>
    suspend fun submitLeaderboardEntry(entry: LeaderBoard): Result<Unit>
    fun getLeaderboard(quizId: String): Flow<List<LeaderBoard>>

    suspend fun submitQuizAndPromote(quizId: String, answers: List<Any>): Result<QuizSubmissionResult>


    suspend fun getAdmins(): Result<List<Map<String, Any>>>
    suspend fun addAdmin(email: String, role: String): Result<Unit>
    suspend fun removeAdmin(uid: String): Result<Unit>



}