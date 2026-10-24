package com.example.domain.use_cases.categories

import com.example.domain.module.ContentCategory
import com.example.domain.repository.CategoryRepository
import javax.inject.Inject

/** Creates a category or saves a renamed, re-described or reordered one. */
class SaveCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: ContentCategory): Result<Unit> = repository.saveCategory(category)
}
