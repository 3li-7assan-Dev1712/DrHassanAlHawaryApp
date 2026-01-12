package com.example.feature.article.domain.use_case

import androidx.paging.PagingData
import com.example.domain.module.Article
import com.example.feature.article.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaginatedArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {

    operator fun invoke(query: String): Flow<PagingData<Article>> {
        return articleRepository.getPaginatedArticles(query)

    }

}