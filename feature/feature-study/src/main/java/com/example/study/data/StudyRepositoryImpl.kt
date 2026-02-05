package com.example.study.data

import android.util.Log
import com.example.data_firebase.StudentFirestoreSource
import com.example.data_local.LessonDao
import com.example.data_local.LevelsDao
import com.example.data_local.LocalDataStore
import com.example.data_local.PlaylistDao
import com.example.data_local.StudentDao
import com.example.domain.module.Lesson
import com.example.domain.module.Level
import com.example.domain.module.Playlist
import com.example.study.data.mappers.toDomain
import com.example.study.data.mappers.toEntity
import com.example.study.domain.model.Student
import com.example.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val studentFirestoreSource: StudentFirestoreSource,
    private val studentDao: StudentDao,
    private val versionStore: LocalDataStore,
    private val playlistDao: PlaylistDao,
    private val lessonDao: LessonDao,
    private val levelsDao: LevelsDao,
    private val fileDownloader: FileDownloader
) : StudyRepository {


    val TAG = "StudyRepositoryImpl"

    override fun getStudentData(): Flow<Student?> {
        return studentDao.getCurrentStudentData().map {
            it?.toDomain()
        }
    }

    override suspend fun disconnectTelegram() {

        // load the student info
        val telegramId = studentDao.getCurrentStudentData().last()?.telegramId
        if (telegramId != null) {
            studentFirestoreSource.updateStudentConnectionStatus(telegramId, false)
            studentDao.deleteStudentByTelegramId(telegramId)
        } else
            throw NullPointerException("Student data is null")
    }


    override suspend fun saveStudentData(telegramId: Long) {
        Log.d(TAG, "saveStudentData: telegram id = $telegramId")
        val studentData = studentFirestoreSource.getStudentByTelegramId(telegramId)?.toEntity()
        if (studentData != null) {
            studentDao.storeStudent(studentData)
        }


    }

    override fun getPlaylistsForLevel(levelId: String): Flow<List<Playlist>?> {

        return playlistDao.getPlaylistsForLevel(levelId).map { playlistsEntities ->
            playlistsEntities?.map { playlistEntity ->
                playlistEntity.toDomain()
            }
        }
    }

    override suspend fun syncPlaylists() {
        val lastPlaylistSync = versionStore.getLastPlaylistSync()
        val playlists = studentFirestoreSource.getPlaylists(lastPlaylistSync)

        Log.d(TAG, "syncPlaylists: ${playlists.size}")
        if (playlists.isNotEmpty()) {
            val entities = playlists.map { it.toEntity() }
            playlistDao.upsertAll(entities)
            versionStore.setLastPlaylistSync(
                entities.maxOf { it.updatedAt }
            )
        }

    }


    override suspend fun syncLevels() {
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


    }

    override fun getLevels(): Flow<List<Level>> =
        levelsDao.getLevels().map {
            it?.map { levelEntity ->
                levelEntity.toDomain()
            } ?: emptyList()
        }

    override fun getLessonsForPlaylist(playlistId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsForPlaylist(playlistId).map { lessonsEntities ->
            lessonsEntities?.map { lessonEntity ->
                lessonEntity.toDomain()
            } ?: emptyList()
        }

    override suspend fun syncLessons() {
        val lastLessonSync = versionStore.getLastLessonSync()
        val lessons = studentFirestoreSource.getUpdatedLessons(lastLessonSync)
        if (lessons.isNotEmpty()) {
            val entities = lessons.map {
                it.toEntity()
            }
            lessonDao.upsertAll(entities)
            versionStore.setLastLessonSync(
                entities.maxOf { it.updatedAt }
            )

        }
    }

    override fun getLessonById(lessonId: String): Flow<Lesson?> =
        lessonDao.getLessonById(lessonId).map {
            it?.toDomain()
        }


    override suspend fun ensureLessonFilesDownloaded(id: String) {
        val entity = lessonDao.getLessonById(id).first() ?: return


        val audioFilePath = entity.audioFilePath
            ?: entity.audioRemoteUrl.let {
                fileDownloader.downloadAudio(it, entity.id)
            }

        val pdfFilePath = entity.pdfFilePath
            ?: entity.pdfRemoteUrl.let {
                fileDownloader.downloadPdf(it, entity.id)
            }


        lessonDao.updateLessonFiles(
            id,
            audioFilePath,
            pdfFilePath
        )
    }


}