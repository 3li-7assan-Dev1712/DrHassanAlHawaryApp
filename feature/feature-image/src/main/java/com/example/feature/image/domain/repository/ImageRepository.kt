package com.example.feature.image.domain.repository

import androidx.paging.PagingData
import com.example.feature.image.domain.model.ImageGroup
import kotlinx.coroutines.flow.Flow

interface ImageRepository {


    fun getPaginatedImageGroups(): Flow<PagingData<ImageGroup>>

}