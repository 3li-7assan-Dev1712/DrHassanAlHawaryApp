package com.example.domain.use_cases.videos

import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.repository.VideosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideoCategoriesUseCase @Inject constructor(
    private val repository: VideosRepository
) {
    operator fun invoke(): Flow<List<ContentCategory>> {
        return repository.getCategories(ContentType.VIDEO)
    }
}
