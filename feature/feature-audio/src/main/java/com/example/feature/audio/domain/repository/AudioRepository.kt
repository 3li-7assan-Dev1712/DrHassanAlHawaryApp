package com.example.feature.audio.domain.repository

import androidx.paging.PagingData
import com.example.feature.audio.domain.model.Audio
import kotlinx.coroutines.flow.Flow

interface AudioRepository {


    fun getPaginatedAudio(query: String): Flow<PagingData<Audio>>

}