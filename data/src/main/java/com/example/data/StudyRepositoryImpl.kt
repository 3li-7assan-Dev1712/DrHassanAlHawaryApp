package com.example.data

import android.util.Log
import com.example.data.mappers.toDomain
import com.example.data.mappers.toDto
import com.example.data.mappers.toEntity
import com.example.data_firebase.GoogleAuthUiClient
import com.example.data_firebase.StudentFirestoreSource
import com.example.data_local.LessonDao
import com.example.data_local.LevelsDao
import com.example.data_local.LocalDataStore
import com.example.data_local.PlaylistDao
import com.example.data_local.StudentDao
import com.example.domain.module.LeaderBoard
import com.example.domain.module.Lesson
import com.example.domain.module.Level
import com.example.domain.module.Playlist
import com.example.domain.module.Quiz
import com.example.domain.module.QuizSubmissionResult
import com.example.domain.module.Student
import com.example.domain.module.UserData
import com.example.domain.repository.StudyRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val studentFirestoreSource: StudentFirestoreSource,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val studentDao: StudentDao,
    private val versionStore: LocalDataStore,
    private val playlistDao: PlaylistDao,
    private val lessonDao: LessonDao,
    private val levelsDao: LevelsDao,
    private val fileDownloader: FileDownloader,
) : StudyRepository {

    override suspend fun getStudentAuthData(): UserData? {
        return googleAuthUiClient.getUserData()
    }

    val TAG = "StudyRepositoryImpl"

    override fun getStudentData(): Flow<Student?> {
        return studentDao.getCurrentStudentData().map {
            it?.toDomain()
        }
    }

    override suspend fun deleteStudentData() {
        studentDao.deleteAll()
    }

    override suspend fun disconnectTelegram() {

        // load the student info
        val telegramId = studentDao.getCurrentStudentData().last()?.telegramId
        if (telegramId != null) {
            studentFirestoreSource.updateStudentConnectionStatus(telegramId, false)
            studentDao.deleteStudentByTelegramId(telegramId)
        }
    }

    override suspend fun checkMembership(uid: String, telegramId: Long): Result<Unit> {
        return studentFirestoreSource.checkMembership(uid, telegramId)
    }


    override fun getPlaylistsForLevel(levelId: String): Flow<List<Playlist>?> {

        return playlistDao.getPlaylistsForLevel(levelId).map { playlistsEntities ->
            playlistsEntities?.filter { !it.isDeleted }?.map { playlistEntity ->
                playlistEntity.toDomain()
            }
        }
    }

    override suspend fun syncPlaylists() {
        try {
            val lastPlaylistSync = versionStore.getLastPlaylistSync()
            val playlists = studentFirestoreSource.getUpdatedPlaylists(lastPlaylistSync)

            Log.d(TAG, "syncPlaylists: ${playlists.size}")
            if (playlists.isNotEmpty()) {
                val existingLevelIds = levelsDao.getAllIds().toSet()

                // Filter out playlists whose level doesn't exist locally to avoid FK constraint failure
                val validEntities = playlists
                    .map { it.toEntity() }
                    .filter { it.levelId in existingLevelIds }

                if (validEntities.isNotEmpty()) {
                    playlistDao.upsertAll(validEntities)
                    versionStore.setLastPlaylistSync(
                        validEntities.maxOf { it.updatedAt }
                    )
                    playlists.forEach { playlist ->
                        if(playlist.isDeleted) {
                            playlistDao.deletePlaylistById(playlist.id)
                            return@forEach
                        }
                    }
                }

                if (validEntities.size < playlists.size) {
                    Log.w(
                        TAG,
                        "syncPlaylists: Ignored ${playlists.size - validEntities.size} playlists due to missing levels."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncPlaylists failed", e)
        }
    }


    override suspend fun syncLevels() {
        try {
            val localLevelVersion = versionStore.getLevelsVersion()
            val remoteLevelVersion = studentFirestoreSource.getLevelsVersion()

            val counts = levelsDao.count()
            Log.d(TAG, "syncLevels: count: $counts")
            if (remoteLevelVersion == localLevelVersion && counts > 0)
                return

            val levels = studentFirestoreSource.getRemoteLevels()
            Log.d(TAG, "syncLevels: count: ${levels.count()}")
            levelsDao.storeLevels(levels.map { it.toEntity() })
            versionStore.updateLevelsVersion(remoteLevelVersion)
        } catch (e: Exception) {
            Log.e(TAG, "syncLevels failed", e)
        }
    }

    override fun getLevels(): Flow<List<Level>> =
        levelsDao.getLevels().map {
            it?.map { levelEntity ->
                levelEntity.toDomain()
            } ?: emptyList()
        }

    override fun getLessonsForPlaylist(playlistId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsForPlaylist(playlistId).map { lessonsEntities ->
            lessonsEntities?.filter { !it.isDeleted }?.map { lessonEntity ->
                lessonEntity.toDomain()
            } ?: emptyList()
        }

    override suspend fun syncLessons() {
        try {
            val lastLessonSync = versionStore.getLastLessonSync()
            val lessons = studentFirestoreSource.getUpdatedLessons(lastLessonSync)
            Log.d(TAG, "syncLessons: lessons: ${lessons.size}")
            if (lessons.isNotEmpty()) {
                val existingPlaylistIds = playlistDao.getAllIds().toSet()

                // Filter out lessons whose playlist doesn't exist locally to avoid FK constraint failure
                val entities = lessons
                    .map { it.toEntity() }
                    .filter { it.playlistId in existingPlaylistIds }

                if (entities.isNotEmpty()) {

                    lessonDao.upsertAll(entities)
                    versionStore.setLastLessonSync(
                        entities.maxOf { it.updatedAt }
                    )
                    entities.forEach { entity ->
                        if (entity.isDeleted) {
                            lessonDao.deleteLessonById(entity.id)
                            return@forEach
                        } else {
                            ensureLessonFilesDownloaded(entity.id)
                        }
                    }
                }

                if (entities.size < lessons.size) {
                    Log.w(
                        TAG,
                        "syncLessons: Ignored ${lessons.size - entities.size} lessons due to missing playlists."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncLessons failed", e)
        }
    }

    override fun getLessonById(lessonId: String): Flow<Lesson?> =
        lessonDao.getLessonById(lessonId).map {
            it?.toDomain()
        }


    override suspend fun ensureLessonFilesDownloaded(id: String) {
        try {
            val entity = lessonDao.getLessonById(id).first() ?: return


            // We let fileDownloader check if the specific version for THIS URL exists.
            val audioFilePath = fileDownloader.downloadAudio(entity.audioRemoteUrl, entity.id)
            val pdfFilePath = fileDownloader.downloadPdf(entity.pdfRemoteUrl, entity.id)

            lessonDao.updateLessonFiles(id, audioFilePath, pdfFilePath)
        } catch (e: Exception) {
            Log.e(TAG, "ensureLessonFilesDownloaded failed for $id", e)
        }
    }


    override suspend fun saveStudentData(uid: String) {

        val studentData = studentFirestoreSource.getStudentDataById(uid)?.toEntity()
        if (studentData != null) {
            studentDao.deleteAll()
            studentDao.storeStudent(studentData)
        }


    }

    override suspend fun storeAdminDataToRoom(telegramId: Long) {
        Log.d(TAG, "saveStudentData: telegram id = $telegramId")
        val studentData = studentFirestoreSource.getAdminDataByTelegramId(telegramId)?.toEntity()
        if (studentData != null) {
            studentDao.storeStudent(studentData)
        }


    }

    // admin
    override suspend fun getRemotePlaylistForLevel(levelId: String): List<Playlist> {
        val playlists = studentFirestoreSource.getRemotePlaylistForLevel(levelId)
        return playlists.map {
            it.toDomain()
        }
    }

    override suspend fun getRemotePlaylistById(playlistId: String): Playlist? {

        return studentFirestoreSource.getRemotePlaylistById(playlistId)?.toDomain()
    }

    override suspend fun uploadPlaylist(playlist: Playlist): Flow<UploadResult> {
        return studentFirestoreSource.uploadPlaylist(playlist.toDto())
    }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> {
        return studentFirestoreSource.deletePlaylist(playlistId)
    }

    override suspend fun updatePlaylist(
        playlistId: String,
        newTitle: String,
        newLevelId: String,
        newOrder: Int,
        newThumbnailLocalOrRemote: String?
    ): Result<String> {
        return studentFirestoreSource.updatePlaylist(
            playlistId = playlistId,
            newTitle = newTitle,
            newLevelId = newLevelId,
            newOrder = newOrder,
            newThumbnailLocalOrRemote = newThumbnailLocalOrRemote
        )
    }

    override suspend fun getRemoteLessonsForPlaylist(playlistId: String): List<Lesson> {
        val lessons = studentFirestoreSource.getRemoteLessonsForPlaylist(playlistId)
        return lessons.map {
            it.toDomain()
        }
    }

    override suspend fun updateLesson(
        lesson: Lesson,
        localAudioUrl: String?,
        localPdfUrl: String?
    ): Result<String> {
        return studentFirestoreSource.updateLesson(
            newTitle = lesson.title,
            newOrder = lesson.order,
            localAudioUrl = localAudioUrl,
            localPdfUrl = localPdfUrl,
            lessonId = lesson.id
        )
    }

    override suspend fun addLesson(
        lesson: Lesson,
        playlistId: String,
    ): Flow<UploadResult> {
        Log.d(TAG, "addLesson: playlistId: $playlistId")
        return studentFirestoreSource.addLesson(
            lesson.toDto(),
            playlistId = playlistId,
        )
    }

    override suspend fun deleteLesson(lessonId: String): Result<Unit> {
        return studentFirestoreSource.deleteLesson(lessonId)
    }

    override suspend fun getRemoteMotivationalMessages(): List<String> {
        val messages = studentFirestoreSource.getRemoteMotivationalMessages()
        return messages
    }

    override suspend fun getRemoteLessonById(lessonId: String): Lesson? {
        val lesson = studentFirestoreSource.getRemoteLessonById(lessonId)
        return lesson?.toDomain()
    }

    override suspend fun getLatestQuiz(batchId: String): Quiz? {
        return studentFirestoreSource.getLatestQuiz(batchId)?.toDomain()
    }

    override suspend fun getQuizWithQuestions(batchId: String): Quiz? {
        val pair = studentFirestoreSource.getQuizWithQuestions(batchId) ?: return null
        val quizDto = pair.first
        val questionsDto = pair.second

        return quizDto.copy(questions = questionsDto).toDomain()
    }

    override suspend fun uploadQuiz(quiz: Quiz): Result<Unit> {
        return studentFirestoreSource.uploadQuiz(quiz.toDto())
    }
    
    override suspend fun getAllQuizzes(): List<Quiz> {
        return studentFirestoreSource.getAllQuizzes().map { it.toDomain() }
    }
    
    override suspend fun updateQuizControls(quizId: String, isActive: Boolean, startAt: Long?, endAt: Long?): Result<Unit> {
        return studentFirestoreSource.updateQuizControls(quizId, isActive, startAt, endAt)
    }

    override suspend fun submitLeaderboardEntry(entry: LeaderBoard): Result<Unit> {
        return try {
            studentFirestoreSource.submitLeaderboardEntry(entry.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLeaderboard(quizId: String): Flow<List<LeaderBoard>> {
        return studentFirestoreSource.getLeaderboardFlow(quizId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun submitQuizAndPromote(quizId: String, answers: List<Any>): Result<QuizSubmissionResult> {
        return try {
            // 1. Call cloud function
            val response = studentFirestoreSource.submitQuizAndPromote(quizId, answers)
            
            val submissionResult = QuizSubmissionResult(
                score = (response["score"] as? Number)?.toInt() ?: 0,
                total = (response["total"] as? Number)?.toInt() ?: 0,
                passed = response["passed"] as? Boolean ?: false,
                newLevelId = response["newLevelId"] as? String
            )

            // 2. If promoted, update local state
            val newLevelId = submissionResult.newLevelId
            if (submissionResult.passed && newLevelId != null) {
                val currentStudent = studentDao.getCurrentStudentData().first()
                if (currentStudent != null) {
                    val updatedStudent = currentStudent.copy(
                        currentLevelId = newLevelId
                    )
                    studentDao.storeStudent(updatedStudent)
                }
            }

            Result.success(submissionResult)
        } catch (e: Exception) {
            Log.e(TAG, "submitQuizAndPromote failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getAdmins(): Result<List<Map<String, Any>>> =
        studentFirestoreSource.getAdmins()


    override suspend fun addAdmin(email: String, role: String): Result<Unit> =
        studentFirestoreSource.addAdmin(email, role)

    override suspend fun removeAdmin(uid: String): Result<Unit> =
        studentFirestoreSource.removeAdmin(uid)


}
