package com.example.domain.use_cases.audios

import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateAudioUseCase @Inject constructor(
    private val repository: AudiosRepository
) {
    suspend operator fun invoke(
        id: String,
        title: String,
        newUriString: String?,
        existingUrl: String,
        durationInMillis: Long,
        type: String? = null
    ): Flow<UploadResult> {
        return repository.updateAudio(id, title, newUriString, existingUrl, durationInMillis, type)
    }
}
