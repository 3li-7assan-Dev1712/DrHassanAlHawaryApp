package com.example.feature.video.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data_firebase.VideoFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.VideoEntity
import com.example.domain.module.NetworkStatus
import com.example.domain.use_cases.GetCurrentNetworkStatusUseCase
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class VideoRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val videoFirestoreSource: VideoFirestoreSource,
    private val networkStatusUseCase: GetCurrentNetworkStatusUseCase,
) : RemoteMediator<Int, VideoEntity>() {

    private val videoDao = appDatabase.videoDao()
    private val TAG = "VideoRemoteMediator"

    override suspend fun initialize(): InitializeAction {
        // LAUNCH_INITIAL_REFRESH ensures we check for new content on every app session.
        // Paging 3 will display local Room data while the refresh is happening.
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, VideoEntity>
    ): MediatorResult {
        return try {
            val lastPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    
                    if (networkStatusUseCase().first() == NetworkStatus.Unavailable) {
                        if (lastItem == null) {
                            return MediatorResult.Success(endOfPaginationReached = true)
                        } else {
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
                    lastItem?.publishDate
                }
            }

            // Fetch from Firebase using publishDate for ordering and pagination cursor
            val videosFromServer = videoFirestoreSource.fetchVideoPage(
                startAfterPublishDate = lastPublishDate,
                limit = state.config.pageSize
            )

            val endOfPaginationReached = videosFromServer.size < state.config.pageSize

            appDatabase.withTransaction {
                // IMPORTANT: We do NOT clearAll() here anymore.
                // We only upsert the new/updated items to support offline persistence.
                
                val entities = videosFromServer.map {
                    VideoEntity(
                        id = it.id,
                        title = it.title,
                        videoUrl = it.videoUrl,
                        publishDate = it.publishDate.time,
                        youtubeVideoId = it.youtubeVideoId,
                        updatedAt = it.publishDate.time,
                        isDeleted = false
                    )
                }

                videoDao.upsertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
