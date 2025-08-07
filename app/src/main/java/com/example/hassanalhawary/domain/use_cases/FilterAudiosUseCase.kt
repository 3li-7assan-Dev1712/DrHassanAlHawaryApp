package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.repository.AudiosRepository
import javax.inject.Inject

class FilterAudiosUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository
) {


    operator fun invoke(audios: List<Audio>, query: String): List<Audio> {
        return audiosRepository.filterAudios(audios, query)
    }
}