package com.example.domain.use_cases.images

import com.example.domain.module.ImageGroup
import com.example.domain.repository.ImagesRepository
import javax.inject.Inject

class GetAllRemoteImageGroupsUseCase @Inject constructor(
    private val repository: ImagesRepository
) {
    suspend operator fun invoke(): List<ImageGroup> {
        return repository.getAllRemoteImageGroups()
    }
}
