package com.example.feature.audio.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.AudioEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject


@OptIn(ExperimentalPagingApi::class)
class AudioRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firestoreSource: AudioFirestoreSource,
    private val networkStatusUseCase: GetCurrentNetworkStatusUseCase
) : RemoteMediator<Int, AudioEntity>() {

    private val audioDao = appDatabase.audioDao()
    private val TAG = "AudioRemoteMediator"

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AudioEntity>
    ): MediatorResult {
        return try {
            val initialPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastLocalItem = state.lastItemOrNull()
                    
                    if (networkStatusUseCase().first() == NetworkStatus.Unavailable) {
                        if (lastLocalItem == null) {
                            return MediatorResult.Success(endOfPaginationReached = true)
                        } else {
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
                    lastLocalItem?.publishDate
                }
            }

            var currentLastPublishDate = initialPublishDate
            var lastResultEndOfPaginationReached = false

            while (true) {
                val audiosFromServer = firestoreSource.fetchAudioPage(
                    startAfterPublishDate = currentLastPublishDate,
                    limit = state.config.pageSize
                )

                lastResultEndOfPaginationReached = audiosFromServer.size < state.config.pageSize

                if (audiosFromServer.isEmpty()) break

                val (deletedItems, activeItems) = audiosFromServer.partition { it.isDeleted }

                appDatabase.withTransaction {
                    deletedItems.forEach { dto ->
                        audioDao.deleteById(dto.id)
                    }

                    if (activeItems.isNotEmpty()) {
                        val serverAudioIds = activeItems.map { it.id }
                        val localAudiosMap = audioDao.getAudiosByIds(serverAudioIds).associateBy { it.id }

                        val mergedEntities = activeItems.map { dto ->
                            val localAudio = localAudiosMap[dto.id]

                            val serverEntity = AudioEntity(
                                id = dto.id,
                                title = dto.title,
                                audioUrl = dto.audioUrl,
                                durationInMillis = dto.durationInMillis,
                                publishDate = dto.publishDate?.toDate()?.time ?: 0L,
                                updatedAt = dto.updatedAt?.toDate()?.time ?: 0L,
                                isDeleted = dto.isDeleted,
                                type = dto.type
                            )

                            if (localAudio != null) {
                                serverEntity.copy(
                                    isFavorite = localAudio.isFavorite,
                                    lastPlayedTimestamp = localAudio.lastPlayedTimestamp,
                                    localFilePath = localAudio.localFilePath,
                                    isDownloaded = localAudio.isDownloaded,
                                )
                            } else {
                                serverEntity
                            }
                        }
                        audioDao.upsertAll(mergedEntities)
                    }
                }

                if (activeItems.isNotEmpty() || lastResultEndOfPaginationReached) {
                    break
                }
                currentLastPublishDate = audiosFromServer.last().publishDate?.toDate()?.time
            }

            MediatorResult.Success(endOfPaginationReached = lastResultEndOfPaginationReached)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
