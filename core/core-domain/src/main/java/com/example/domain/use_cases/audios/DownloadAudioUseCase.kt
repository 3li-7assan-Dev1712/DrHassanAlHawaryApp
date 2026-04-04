package com.example.domain.use_cases.audios

import com.example.domain.module.Audio
import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed class DownloadResult {
    data class Progress(val percentage: Int) : DownloadResult()
    data class Success(val localPath: String) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

class DownloadAudioUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository
) {
    suspend operator fun invoke(audio: Audio): Flow<DownloadResult> {
        return audiosRepository.downloadAudio(audio)
    }
}
