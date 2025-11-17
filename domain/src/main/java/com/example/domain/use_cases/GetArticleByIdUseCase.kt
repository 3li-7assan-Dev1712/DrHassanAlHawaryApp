package com.example.domain.use_cases



import com.example.domain.module.ArticlesResult
import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject

class GetArticleByIdUseCase @Inject constructor(
    private val articlesRepository: ArticlesRepository
) {


    suspend operator fun invoke(articleId: String): ArticlesResult {
        return articlesRepository.getArticleById(articleId)
    }
}