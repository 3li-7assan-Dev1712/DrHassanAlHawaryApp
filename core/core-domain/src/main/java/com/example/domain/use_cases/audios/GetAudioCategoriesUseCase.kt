package com.example.domain.use_cases.audios

import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioCategoriesUseCase @Inject constructor(
    private val repository: AudiosRepository
) {
    operator fun invoke(): Flow<List<ContentCategory>> {
        return repository.getCategories(ContentType.AUDIO)
    }
}
