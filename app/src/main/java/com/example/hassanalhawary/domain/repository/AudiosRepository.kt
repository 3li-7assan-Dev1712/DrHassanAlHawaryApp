package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.AudiosResult

interface AudiosRepository {


    fun filterAudios(audios: List<Audio>, query: String): List<Audio>

    suspend fun getAllAudios(): AudiosResult

}