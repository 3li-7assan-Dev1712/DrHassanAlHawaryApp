package com.example.domain.use_cases.categories

import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** [type]'s categories in display order (the server's, or the built-in ones). */
class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(type: ContentType): Flow<List<ContentCategory>> = repository.getCategories(type)
}
