package com.example.domain.use_cases.images

import com.example.domain.repository.ImagesRepository
import javax.inject.Inject

class DeleteImageGroupUseCase @Inject constructor(
    private val repository: ImagesRepository
) {
    suspend operator fun invoke(groupId: String): Result<Unit> {
        return repository.deleteImageGroup(groupId)
    }
}
