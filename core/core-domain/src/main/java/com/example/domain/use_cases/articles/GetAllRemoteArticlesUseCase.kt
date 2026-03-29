package com.example.domain.use_cases.articles

import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject

class GetAllRemoteArticlesUseCase @Inject constructor(
    private val repository: ArticlesRepository
) {
    suspend operator fun invoke(): List<Article> {
        return repository.getAllRemoteArticles()
    }
}
