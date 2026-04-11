package com.example.feature.audio.domain.use_case


import androidx.paging.PagingData
import com.example.domain.module.Audio
import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedAudioUseCase @Inject constructor(
    private val audioRepository: AudiosRepository
) {

    operator fun invoke(query: String): Flow<PagingData<Audio>> {
        return audioRepository.getPaginatedAudio(query)

    }

}