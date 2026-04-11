package com.example.feature.image.domain.use_case

import androidx.paging.PagingData
import com.example.domain.module.ImageGroup
import com.example.domain.repository.ImagesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImageGroupsUseCase @Inject constructor(
    private val imageRepository: ImagesRepository
) {

    /**
     * Executes the use case to get a flow of all image groups.
     */
    operator fun invoke(): Flow<PagingData<ImageGroup>> {
        return imageRepository.getPaginatedImageGroups()
    }
}