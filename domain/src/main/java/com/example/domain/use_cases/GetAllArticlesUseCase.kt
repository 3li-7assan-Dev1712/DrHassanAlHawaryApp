package com.example.domain.use_cases


import com.example.domain.module.ArticlesResult
import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject


class GetAllArticlesUseCase @Inject constructor(
    private val articlesRepository: ArticlesRepository
) {


    suspend operator fun invoke(): ArticlesResult {
        return articlesRepository.getArticlesFromServer()
    }
}