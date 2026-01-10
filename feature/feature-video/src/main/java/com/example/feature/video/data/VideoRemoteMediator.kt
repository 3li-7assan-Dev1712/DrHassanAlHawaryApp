package com.example.feature.video.data


import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data_firebase.FirebaseMediaSource
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
    private val firebaseMediaSource: FirebaseMediaSource,
    private val networkStatusUseCase: GetCurrentNetworkStatusUseCase,
) : RemoteMediator<Int, VideoEntity>() {

    private val videoDao = appDatabase.videoDao()
    private val TAG = "VideoRemoteMediator"


    override suspend fun initialize(): InitializeAction {
        return if (videoDao.count() > 0) {
            Log.d(TAG, "DB has data. Skipping remote refresh on launch.")
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            Log.d(TAG, "DB is empty. Launching remote refresh on launch.")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, VideoEntity>
    ): MediatorResult {
        return try {
            val lastItemKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // Logic to get the last item for pagination key
                    val lastLocalItem = videoDao.getLastVideo()

                    // Handle Offline/Flag Logic
                    if (networkStatusUseCase().first() == NetworkStatus.Unavailable) {
                        if (lastLocalItem == null) {
                            Log.d(TAG, "Offline/Flag True: Local data exhausted.")
                            return MediatorResult.Success(endOfPaginationReached = true)
                        } else {
                            Log.d(TAG, "Offline/Flag True: Letting Room continue paging.")
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }

                    // Online: return the ID of the last item to start fetching after it
                    lastLocalItem?.id
                }
            }

            // Fetch from Firebase
            val videosFromServer = firebaseMediaSource.fetchVideoPage(
                startAfterKey = lastItemKey,
                limit = state.config.pageSize
            )

            val endOfPaginationReached = videosFromServer.size < state.config.pageSize

            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    videoDao.clearAll()
                }

                val entities = videosFromServer.map {

                    VideoEntity(
                        id = it.id,
                        title = it.title,
                        videoUrl = it.videoUrl,
                        publishDate = it.publishDate.time,
                        youtubeVideoId = it.youtubeVideoId
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