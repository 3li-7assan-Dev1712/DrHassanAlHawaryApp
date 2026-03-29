package com.example.domain.use_cases.audios

import com.example.domain.module.Audio
import com.example.domain.repository.AudiosRepository
import javax.inject.Inject

class GetAudioByIdUseCase @Inject constructor(
    private val repository: AudiosRepository
) {
    suspend operator fun invoke(audioId: String): Audio? {
        return repository.getAudioById(audioId)
    }
}
