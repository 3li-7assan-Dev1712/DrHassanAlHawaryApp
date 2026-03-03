package com.example.data

import com.example.data.mappers.toDomain
import com.example.data.mappers.toDto
import com.example.data.mappers.toEntity
import com.example.data_firebase.StudentFirestoreSource
import com.example.data_local.LessonDao
import com.example.data_local.LevelsDao
import com.example.data_local.LocalDataStore
import com.example.data_local.PlaylistDao
import com.example.data_local.UserDao
import com.example.domain.module.LeaderBoard
import com.example.domain.module.Lesson
import com.example.domain.module.Level
import com.example.domain.module.Playlist
import com.example.domain.module.Quiz
import com.example.domain.module.User
import com.example.domain.repository.StudyRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val studentFirestoreSource: StudentFirestoreSource,
    private val userDao: UserDao,
    private val versionStore: LocalDataStore,
    private val playlistDao: PlaylistDao,
    private val lessonDao: LessonDao,
    private val levelsDao: LevelsDao,
    private val fileDownloader: FileDownloader
) : StudyRepository {

    private val TAG = "StudyRepositoryImpl"

    override fun observeUser(): Flow<User?> {
        return userDao.observeUser().map { it?.toDomain() }
    }

    override suspend fun upsertUser(user: User) {
        // Save to Local (only one user allowed)
        userDao.upsertUser(user.toEntity())
        // Save to Remote
        studentFirestoreSource.upsertUser(user.toDto())
    }

    override suspend fun getUser(): User? {
        return userDao.getUser()?.toDomain()
    }

    override suspend fun getRemoteUser(uid: String): User? {
        return studentFirestoreSource.getUser(uid)?.toDomain(uid)
    }

    override suspend fun clearUser() {
        userDao.clearAll()
    }

    override suspend fun disconnectTelegram(uid: String) {
        studentFirestoreSource.updateTelegramDisconnected(uid)
        val localUser = userDao.getUser()
        if (localUser != null) {
            userDao.upsertUser(localUser.copy(
                isConnectedToTelegram = false,
                telegramId = null,
                telegramUsername = null,
                telegramFirstName = null,
                telegramLastName = null,
                telegramPhotoUrl = null,
                membershipState = "none"
            ))
        }
    }

    override fun getPlaylistsForLevel(levelId: String): Flow<List<Playlist>?> {
        return playlistDao.getPlaylistsForLevel(levelId).map { playlistsEntities ->
            playlistsEntities?.map { it.toDomain() }
        }
    }

    override suspend fun syncPlaylists() {
        val lastPlaylistSync = versionStore.getLastPlaylistSync()
        val playlists = studentFirestoreSource.getUpdatedPlaylists(lastPlaylistSync)
        if (playlists.isNotEmpty()) {
            val entities = playlists.map { it.toEntity() }
            playlistDao.upsertAll(entities)
            versionStore.setLastPlaylistSync(entities.maxOf { it.updatedAt })
        }
    }

    override suspend fun syncLevels() {
        val localLevelVersion = versionStore.getLevelsVersion()
        val remoteLevelVersion = studentFirestoreSource.getLevelsVersion()
        val counts = levelsDao.count()
        if (remoteLevelVersion == localLevelVersion && counts > 0) return

        val levels = studentFirestoreSource.getRemoteLevels()
        levelsDao.storeLevels(levels.map { it.toEntity() })
        versionStore.updateLevelsVersion(remoteLevelVersion)
    }

    override fun getLevels(): Flow<List<Level>> =
        levelsDao.getLevels().map { it?.map { it.toDomain() } ?: emptyList() }

    override fun getLessonsForPlaylist(playlistId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsForPlaylist(playlistId).map { lessonsEntities ->
            lessonsEntities?.map { it.toDomain() } ?: emptyList()
        }

    override suspend fun syncLessons() {
        val lastLessonSync = versionStore.getLastLessonSync()
        val lessons = studentFirestoreSource.getUpdatedLessons(lastLessonSync)
        if (lessons.isNotEmpty()) {
            val entities = lessons.map { it.toEntity() }
            lessonDao.upsertAll(entities)
            versionStore.setLastLessonSync(entities.maxOf { it.updatedAt })
        }
    }

    override fun getLessonById(lessonId: String): Flow<Lesson?> =
        lessonDao.getLessonById(lessonId).map { it?.toDomain() }

    override suspend fun ensureLessonFilesDownloaded(id: String) {
        val entity = lessonDao.getLessonById(id).first() ?: return
        val audioFilePath = entity.audioFilePath
            ?: entity.audioRemoteUrl.let { fileDownloader.downloadAudio(it, entity.id) }
        val pdfFilePath = entity.pdfFilePath
            ?: entity.pdfRemoteUrl.let { fileDownloader.downloadPdf(it, entity.id) }
        lessonDao.updateLessonFiles(id, audioFilePath, pdfFilePath)
    }

    // Admin
    override suspend fun getRemotePlaylistForLevel(levelId: String): List<Playlist> {
        return studentFirestoreSource.getRemotePlaylistForLevel(levelId).map { it.toDomain() }
    }

    override suspend fun getRemotePlaylistById(playlistId: String): Playlist? {
        return studentFirestoreSource.getRemotePlaylistById(playlistId)?.toDomain()
    }

    override suspend fun uploadPlaylist(playlist: Playlist): Flow<UploadResult> {
        return studentFirestoreSource.uploadPlaylist(playlist.toDto())
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
        return studentFirestoreSource.getRemoteLessonsForPlaylist(playlistId).map { it.toDomain() }
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
            remoteAudioUrl = lesson.audioUrl,
            remotePdfUrl = lesson.pdfUrl,
            lessonId = lesson.id
        )
    }

    override suspend fun addLesson(lesson: Lesson, playlistId: String): Flow<UploadResult> {
        return studentFirestoreSource.addLesson(lesson.toDto(), playlistId)
    }

    override suspend fun getRemoteMotivationalMessages(): List<String> {
        return studentFirestoreSource.getRemoteMotivationalMessages()
    }

    override suspend fun getRemoteLessonById(lessonId: String): Lesson? {
        return studentFirestoreSource.getRemoteLessonById(lessonId)?.toDomain()
    }

    override suspend fun getLatestQuiz(): Quiz? {
        return studentFirestoreSource.getLatestQuiz()?.toDomain()
    }

    override suspend fun submitLeaderboardEntry(entry: LeaderBoard): Result<Unit> {
        return try {
            studentFirestoreSource.submitLeaderboardEntry(entry.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLeaderboard(): Flow<List<LeaderBoard>> {
        return studentFirestoreSource.getLeaderboardFlow().map { list ->
            list.map { it.toDomain() }
        }
    }
}
