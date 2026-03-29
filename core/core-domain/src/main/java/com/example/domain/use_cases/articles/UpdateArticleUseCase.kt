package com.example.domain.use_cases.articles

import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject

class UpdateArticleUseCase @Inject constructor(
    private val repository: ArticlesRepository
) {
    suspend operator fun invoke(article: Article) {
        repository.updateArticle(article)
    }
}
