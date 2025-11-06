package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.AudiosResult
import kotlinx.coroutines.flow.Flow

interface AudiosRepository {


    fun filterAudios(audios: List<Audio>, query: String): List<Audio>
    //get the data from the room DB
    suspend fun getAudios(): Flow<List<Audio>>
    // get new data from the firebase server then update the room DB.
    suspend fun syncAudios()

    suspend fun getLatestAudios(): AudiosResult

}