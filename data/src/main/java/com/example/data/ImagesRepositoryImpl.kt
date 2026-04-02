package com.example.data

import com.example.data_firebase.ImageFirestoreSource
import com.example.domain.module.ImageGroup
import com.example.domain.repository.ImagesRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ImagesRepositoryImpl @Inject constructor(
    private val imageFirestoreSource: ImageFirestoreSource
) : ImagesRepository {


    override suspend fun uploadDesignGroup(
        title: String,
        imageUris: List<String>,
    ): Flow<UploadResult> {
        return imageFirestoreSource.uploadImageGroup(title, imageUris)
    }

    override suspend fun getAllRemoteImageGroups(): List<ImageGroup> {
        // fetchImageGroupsPage now returns DTOs via toImageGroupDtoSafe
        // We filter out deleted items and map to domain
        return imageFirestoreSource.fetchImageGroupsPage(null, 100).first
            .filter { !it.isDeleted }
            .map { it }
    }

    override suspend fun deleteImageGroup(groupId: String): Result<Unit> {
        return imageFirestoreSource.deleteImageGroup(groupId)
    }

}
