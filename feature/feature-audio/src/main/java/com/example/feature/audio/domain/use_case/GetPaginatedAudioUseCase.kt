package com.example.feature.audio.domain.use_case


import androidx.paging.PagingData
import com.example.feature.audio.domain.model.Audio
import com.example.feature.audio.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedAudioUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {

    operator fun invoke(query: String): Flow<PagingData<Audio>> {
        return audioRepository.getPaginatedAudio(query)

    }

}