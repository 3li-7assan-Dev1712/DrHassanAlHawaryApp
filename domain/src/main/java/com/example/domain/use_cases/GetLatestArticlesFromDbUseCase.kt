package com.example.domain.use_cases



import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetLatestArticlesFromDbUseCase
@Inject constructor(
    private val articlesRepository: ArticlesRepository
) {
    suspend operator fun invoke(): Flow<List<Article>> {
        return articlesRepository.getLatestArticlesFromDb()
    }
}