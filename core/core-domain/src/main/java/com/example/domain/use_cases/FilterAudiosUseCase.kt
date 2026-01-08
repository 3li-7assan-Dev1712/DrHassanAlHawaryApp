package com.example.domain.use_cases


import com.example.domain.module.Audio
import com.example.domain.repository.AudiosRepository
import javax.inject.Inject

class FilterAudiosUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository
) {


    operator fun invoke(audios: List<Audio>, query: String): List<Audio> {
        return audiosRepository.filterAudios(audios, query)
    }
}