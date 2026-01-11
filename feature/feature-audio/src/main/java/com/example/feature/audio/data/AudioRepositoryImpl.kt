package com.example.feature.audio.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data_firebase.FirebaseMediaSource
import com.example.data_local.AppDatabase
import com.example.feature.audio.data.mapper.toDomain
import com.example.feature.audio.domain.model.Audio
import com.example.feature.audio.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class AudioRepositoryImpl
@Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseMediaSource: FirebaseMediaSource,
    private val audioRemoteMediator: AudioRemoteMediator
) : AudioRepository {


    private val audioDao = appDatabase.audioDao()

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
                    lastPlayedTimestamp = audioEntity.lastPlayedTimestamp,
                    localFilePath = audioEntity.localFilePath
                )
            }
        }
    }


}


