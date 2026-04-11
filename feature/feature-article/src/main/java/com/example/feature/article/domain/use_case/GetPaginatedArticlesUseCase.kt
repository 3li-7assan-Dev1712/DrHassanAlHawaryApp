package com.example.feature.article.domain.use_case

import androidx.paging.PagingData
import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedArticlesUseCase @Inject constructor(
    private val articleRepository: ArticlesRepository
) {

    operator fun invoke(query: String, limit: Int = 15): Flow<PagingData<Article>> {
        return articleRepository.getPaginatedArticles(query, limit)

    }

}