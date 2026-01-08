package com.example.domain.use_cases.images

import com.example.domain.module.ImageGroup
import com.example.domain.repository.ImagesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImageGroupsUseCase @Inject constructor(
    private val imagesRepository: ImagesRepository
) {
    /**
     * Executes the use case to get a flow of all image groups.
     */
    operator fun invoke(): Flow<List<ImageGroup>> {
        return imagesRepository.getImageGroups()
    }
}