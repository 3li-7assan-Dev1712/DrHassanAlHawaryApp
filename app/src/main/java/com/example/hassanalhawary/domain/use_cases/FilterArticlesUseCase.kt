package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.repository.ArticlesRepository
import javax.inject.Inject

class FilterArticlesUseCase @Inject constructor(
    private val articlesRepository: ArticlesRepository
) {


    operator fun invoke(articles: List<Article>, query: String): List<Article> {
        return articlesRepository.filterArticles(articles, query)
    }
}