package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Audio

interface AudiosRepository {


    fun filterAudios(audios: List<Audio>, query: String): List<Audio>


}