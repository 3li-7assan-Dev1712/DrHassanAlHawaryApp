package com.example.feature.article.domain.use_case

import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArticleByIdUseCase @Inject constructor(
    private val articlesRepository: ArticlesRepository
) {


    suspend operator fun invoke(articleId: String): Flow<Article> {
        return articlesRepository.getArticleById(articleId)
    }
}