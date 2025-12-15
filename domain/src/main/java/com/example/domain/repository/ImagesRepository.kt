package com.example.domain.repository

import com.example.domain.module.ImageGroup
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface ImagesRepository {
    suspend fun uploadDesignGroup(title: String, imageUris: List<String>): Flow<UploadResult>


    fun getImageGroups(): Flow<List<ImageGroup>>

}