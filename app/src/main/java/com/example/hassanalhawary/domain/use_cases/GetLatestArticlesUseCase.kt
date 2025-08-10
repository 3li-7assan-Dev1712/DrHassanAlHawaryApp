package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.model.ArticlesResult
import com.example.hassanalhawary.domain.repository.ArticlesRepository
import javax.inject.Inject


class GetLatestArticlesUseCase
@Inject constructor(
    private val articlesRepository: ArticlesRepository
) {
    suspend operator fun invoke(): ArticlesResult {
        return articlesRepository.getLatestArticles()
    }
}