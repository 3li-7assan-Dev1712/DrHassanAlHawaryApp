package com.example.data

import com.example.data.mappers.toDomainModel
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_local.AppDatabase
import com.example.domain.module.Audio
import com.example.domain.module.AudiosResult
import com.example.domain.repository.AudiosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class AudiosRepositoryImpl
@Inject constructor(
    appDatabase: AppDatabase,
    private val audioFirestoreSource: AudioFirestoreSource,
) : AudiosRepository {


    private val audioDao = appDatabase.audioDao()


    override fun filterAudios(audios: List<Audio>, query: String): List<Audio> {

        if (query.isBlank()) {
            return audios
        }
        return audios.filter { audio ->
            audio.title.contains(query, ignoreCase = true)
        }

    }


    override suspend fun getAudiosFromDb(): Flow<List<Audio>> {

        return audioDao.getAudiosFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }


    override suspend fun getAudiosFromServer(): AudiosResult {
        return AudiosResult(emptyList())
    }

    override suspend fun uploadAudio(
        title: String,
        uriString: String,
        durationInMillis: Long
    ): Flow<UploadResult> {
        return audioFirestoreSource.uploadAudio(title, uriString, durationInMillis)
    }

    override suspend fun updateAudio(
        id: String,
        title: String,
        newUriString: String?,
        existingUrl: String,
        durationInMillis: Long
    ): Flow<UploadResult> {
        return audioFirestoreSource.updateAudio(id, title, newUriString, existingUrl, durationInMillis)
    }

    override suspend fun deleteAudio(audioId: String, audioUrl: String): Result<Unit> {
        return audioFirestoreSource.deleteAudio(audioId, audioUrl)
    }

    override suspend fun getAudioById(audioId: String): Audio? {
        return audioFirestoreSource.getAudioById(audioId)
    }

    override suspend fun getAllRemoteAudios(): List<Audio> {
        return audioFirestoreSource.fetchAudioPage(null, 100)
            .filter { !it.isDeleted }
            .map { it.toDomainModel() }
    }
}
