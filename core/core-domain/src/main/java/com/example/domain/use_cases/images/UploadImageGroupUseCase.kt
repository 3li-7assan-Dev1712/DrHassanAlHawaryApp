package com.example.domain.use_cases.images

import com.example.domain.repository.ImagesRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject


/**
 * A use case that encapsulates the business logic for uploading a new design group.
 * It orchestrates uploading images to storage and saving the metadata to the database.
 */
class UploadImageGroupUseCase @Inject constructor(
    private val imageRepository: ImagesRepository // Depends on the repository interface
) {
    suspend operator fun invoke(
        title: String,
        imageUris: List<String>
    ): Flow<UploadResult> {
        // Basic validation
        if (title.isBlank() || imageUris.isEmpty()) {
            return flowOf(UploadResult.Error("Title and images cannot be empty."))
        }
        // Delegate the complex logic to the repository
        return imageRepository.uploadDesignGroup(title, imageUris)
    }
}