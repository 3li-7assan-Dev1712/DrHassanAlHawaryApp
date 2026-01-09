package com.example.feature.image.domain.repository

import androidx.paging.PagingData
import com.example.data_local.model.ImageGroupWithImages
import com.example.feature.image.domain.model.ImageGroup
import kotlinx.coroutines.flow.Flow

interface ImageRepository {


    fun getPaginatedImageGroups(): Flow<PagingData<ImageGroup>>

    fun getImageGroupWithImages(groupId: String): Flow<ImageGroupWithImages?>

}