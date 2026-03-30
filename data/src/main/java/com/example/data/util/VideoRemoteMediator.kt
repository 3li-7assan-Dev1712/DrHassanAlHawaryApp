package com.example.data.util


import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
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
        return if (videoDao.count() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, VideoEntity>
    ): MediatorResult {
        return try {
            val initialPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastLocalItem = videoDao.getLastVideo()

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
                val videosFromServer = videoFirestoreSource.fetchVideoPage(
                    startAfterPublishDate = currentLastPublishDate,
                    limit = state.config.pageSize
                )

                lastResultEndOfPaginationReached = videosFromServer.size < state.config.pageSize

                if (videosFromServer.isEmpty()) break

                val (deletedItems, activeItems) = videosFromServer.partition { it.isDeleted }

                appDatabase.withTransaction {
                    deletedItems.forEach { 
                        videoDao.deleteById(it.id)
                    }

                    if (activeItems.isNotEmpty()) {
                        val entities = activeItems.map { it.toEntity() }
                        videoDao.upsertAll(entities)
                    }
                }

                if (activeItems.isNotEmpty() || lastResultEndOfPaginationReached) {
                    break
                }
                currentLastPublishDate = videosFromServer.last().publishDate?.toDate()?.time
            }

            MediatorResult.Success(endOfPaginationReached = lastResultEndOfPaginationReached)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
