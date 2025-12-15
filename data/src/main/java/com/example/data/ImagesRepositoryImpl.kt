package com.example.data

import com.example.data_firebase.FirebaseMediaSource
import com.example.domain.module.ImageGroup
import com.example.domain.repository.ImagesRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ImagesRepositoryImpl @Inject constructor(
    private val firebaseMediaSource: FirebaseMediaSource
) : ImagesRepository {


    override suspend fun uploadDesignGroup(
        title: String,
        imageUris: List<String>
    ): Flow<UploadResult> {
        return firebaseMediaSource.uploadImageGroup(title, imageUris)

    }



    override fun getImageGroups(): Flow<List<ImageGroup>> {

        return flow {
            val groups = firebaseMediaSource.fetchImageGroups()
            emit(groups)
        }
    }

}