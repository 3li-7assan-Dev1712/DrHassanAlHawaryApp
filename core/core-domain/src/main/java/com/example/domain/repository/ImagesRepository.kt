package com.example.domain.repository

import androidx.paging.PagingData
import com.example.domain.module.ImageGroup
import com.example.domain.module.ImageGroupWithImages
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface ImagesRepository {
    suspend fun uploadDesignGroup(title: String, imageUris: List<String>): Flow<UploadResult>
    suspend fun getAllRemoteImageGroups(): List<ImageGroup>
    suspend fun deleteImageGroup(groupId: String): Result<Unit>

    fun getPaginatedImageGroups(): Flow<PagingData<ImageGroup>>

    fun getImageGroupWithImages(groupId: String): Flow<ImageGroupWithImages?>
}
