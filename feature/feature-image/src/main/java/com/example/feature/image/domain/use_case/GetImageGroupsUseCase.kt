package com.example.feature.image.domain.use_case

import androidx.paging.PagingData
import com.example.feature.image.domain.model.ImageGroup
import com.example.feature.image.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImageGroupsUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {

    /**
     * Executes the use case to get a flow of all image groups.
     */
    operator fun invoke(): Flow<PagingData<ImageGroup>> {
        return imageRepository.getPaginatedImageGroups()
    }
}