package com.example.domain.use_cases.articles

import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject

class DeleteArticleUseCase @Inject constructor(
    private val repository: ArticlesRepository
) {
    suspend operator fun invoke(articleId: String) {
        repository.deleteArticle(articleId)
    }
}
