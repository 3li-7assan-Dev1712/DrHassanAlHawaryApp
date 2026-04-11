package com.example.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.mappers.toDomain
import com.example.data.mappers.toDomainModel
import com.example.data.mappers.toEntity
import com.example.data.util.AudioRemoteMediator
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_local.AppDatabase
import com.example.domain.module.Audio
import com.example.domain.module.AudiosResult
import com.example.domain.repository.AudiosRepository
import com.example.domain.use_cases.audios.DownloadResult
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class AudiosRepositoryImpl
@Inject constructor(
    private val appDatabase: AppDatabase,
    private val audioFirestoreSource: AudioFirestoreSource,
    private val fileDownloader: FileDownloader,
    private val audioRemoteMediator: AudioRemoteMediator
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
            entities.map {
                it.toDomain(
                    isFavorite = it.isFavorite,
                    isDownloaded = it.isDownloaded,
                    lastPlayedTimestamp = it.lastPlayedTimestamp
                )
            }
        }
    }


    override suspend fun getAudiosFromServer(): AudiosResult {
        return AudiosResult(emptyList())
    }

    override suspend fun uploadAudio(
        title: String,
        uriString: String,
        durationInMillis: Long,
        type: String
    ): Flow<UploadResult> {
        return audioFirestoreSource.uploadAudio(title, uriString, durationInMillis, type)
    }

    override suspend fun updateAudio(
        id: String,
        title: String,
        newUriString: String?,
        existingUrl: String,
        durationInMillis: Long,
        type: String?
    ): Flow<UploadResult> {
        return audioFirestoreSource.updateAudio(
            id,
            title,
            newUriString,
            existingUrl,
            durationInMillis,
            type
        )
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

    override suspend fun downloadAudio(audio: Audio): Flow<DownloadResult> {
        return fileDownloader.downloadAudioWithProgress(audio.audioUrl, audio.id)
            .onEach { result ->
                if (result is DownloadResult.Success) {
                    Log.d("AudiosRepositoryImpl", "downloadAudio: file path ${result.localPath}")
                    val entity = audio.toEntity().copy(
                        isDownloaded = true,
                        localFilePath = result.localPath
                    )
                    audioDao.upsertAll(listOf(entity))
                } else {
                    Log.d("AudiosRepositoryImpl", "downloadAudio: failed with ${result.toString()}")
                }
            }
    }

    override fun getAudioByUrl(url: String): Flow<Audio?> {
        return audioDao.getAudioByUrl(url).map {
            it?.toDomain(
                isFavorite = it.isFavorite,
                isDownloaded = it.isDownloaded,
                lastPlayedTimestamp = it.lastPlayedTimestamp
            )
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPaginatedAudio(query: String): Flow<PagingData<Audio>> {
        return Pager(
            config = PagingConfig(
                // Set a page size. This is passed to your RemoteMediator's 'state'.
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = audioRemoteMediator,
            // The PagingSourceFactory ALWAYS points to the local database (Room).
            // The RemoteMediator will fill this database for the PagingSource to read.
            pagingSourceFactory = {
                audioDao.getAudiosPagingSource(query)
            }
        ).flow.map { pagingData ->
            // The data from the PagingSource is ArticleEntity, so we map it to the domain model
            pagingData.map { audioEntity ->
                audioEntity.toDomain(
                    isFavorite = audioEntity.isFavorite,
                    isDownloaded = audioEntity.isDownloaded,
                )
            }
        }
    }


}
