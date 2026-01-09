package com.example.feature.image.domain.use_case

import com.example.data_local.model.ImageGroupWithImages
import com.example.feature.image.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupImagesUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(groupId: String): Flow<ImageGroupWithImages?> {
        return repository.getImageGroupWithImages(groupId)
    }
}