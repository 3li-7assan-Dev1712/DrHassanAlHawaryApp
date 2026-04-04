package com.example.domain.use_cases.audios

import com.example.domain.module.Audio
import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioByUrlUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository
) {
    operator fun invoke(url: String): Flow<Audio?> {
        return audiosRepository.getAudioByUrl(url)
    }
}
