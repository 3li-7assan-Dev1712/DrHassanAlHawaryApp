package com.example.feature.image.domain.use_case

import com.example.domain.module.ImageGroupWithImages
import com.example.domain.repository.ImagesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupImagesUseCase @Inject constructor(
    private val repository: ImagesRepository
) {
    operator fun invoke(groupId: String): Flow<ImageGroupWithImages?> {
        return repository.getImageGroupWithImages(groupId)
    }
}