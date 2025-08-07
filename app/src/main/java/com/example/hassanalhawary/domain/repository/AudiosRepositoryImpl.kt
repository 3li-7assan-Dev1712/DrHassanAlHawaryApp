package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Audio
import javax.inject.Inject


class AudiosRepositoryImpl

    @Inject constructor(

    )
    : AudiosRepository {



    override fun filterAudios(audios: List<Audio>, query: String): List<Audio> {

        if (query.isBlank()) {
            return audios
        }
        return audios.filter { audio ->
            audio.title.contains(query, ignoreCase = true)
        }

    }
}


