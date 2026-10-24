package com.example.domain.use_cases.categories

import com.example.domain.module.ContentCategory
import com.example.domain.repository.CategoryRepository
import javax.inject.Inject

/** Deletes a category no item uses any more. */
class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: ContentCategory): Result<Unit> = repository.deleteCategory(category)
}
