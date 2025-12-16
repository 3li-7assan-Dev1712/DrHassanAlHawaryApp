package com.example.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.util.ImageGroupRemoteMediator
import com.example.data_firebase.FirebaseMediaSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.toDomainModel
import com.example.domain.module.ImageGroup
import com.example.domain.repository.ImagesRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class ImagesRepositoryImpl @Inject constructor(
    private val firebaseMediaSource: FirebaseMediaSource,
    private val appDatabase: AppDatabase
) : ImagesRepository {


    override suspend fun uploadDesignGroup(
        title: String,
        imageUris: List<String>
    ): Flow<UploadResult> {
        return firebaseMediaSource.uploadImageGroup(title, imageUris)

    }



    /**
     * Gets a Flow of PagingData for ImageGroups.
     * It uses a RemoteMediator to orchestrate fetching from Firebase and caching in Room.
     */
    @OptIn(ExperimentalPagingApi::class)
    fun getPaginatedImagesGroup(): Flow<PagingData<ImageGroup>> {
        val imageDao = appDatabase.imageDao() // Get DAO from the database
        val pagingSourceFactory = { imageDao.pagingSource() }

        return Pager(
            config = PagingConfig(
                pageSize = 20, // Define how many items to load at once
                enablePlaceholders = false
            ),
            remoteMediator = ImageGroupRemoteMediator(
                firebaseMediaSource = firebaseMediaSource,
                appDatabase = appDatabase
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() } // Map from Entity to Domain model
        }
    }

    override fun getImageGroups(): Flow<List<ImageGroup>> {

        return flow {
            val groups = firebaseMediaSource.fetchImageGroups()
            emit(groups)
        }
    }

}