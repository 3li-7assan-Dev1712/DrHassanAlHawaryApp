package com.example.domain.use_cases.audios

import com.example.domain.repository.AudiosRepository
import javax.inject.Inject

class DeleteAudioUseCase @Inject constructor(
    private val repository: AudiosRepository
) {
    suspend operator fun invoke(audioId: String, audioUrl: String): Result<Unit> {
        return repository.deleteAudio(audioId, audioUrl)
    }
}
