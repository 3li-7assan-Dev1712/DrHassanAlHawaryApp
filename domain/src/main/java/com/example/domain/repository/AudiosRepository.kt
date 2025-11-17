package com.example.domain.repository

import com.example.domain.module.Audio
import com.example.domain.module.AudiosResult
import kotlinx.coroutines.flow.Flow

interface AudiosRepository {


    fun filterAudios(audios: List<Audio>, query: String): List<Audio>

    suspend fun getAudiosFromDb(): Flow<List<Audio>>

    suspend fun syncAudiosDbWithServer()

    suspend fun getAudiosFromServer(): AudiosResult

}