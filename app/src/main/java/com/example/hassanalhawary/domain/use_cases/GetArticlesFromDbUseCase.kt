package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArticlesFromDbUseCase @Inject constructor(
    private val articlesRepository: ArticlesRepository
) {

    suspend operator fun invoke(): Flow<List<Article>> {
        return articlesRepository.getArticlesFromDb()

    }

}