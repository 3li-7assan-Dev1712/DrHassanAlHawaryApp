package com.example.feature.video.domain.repository

import androidx.paging.PagingData
import com.example.feature.video.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {


    fun getPaginatedVideo(): Flow<PagingData<Video>>


}