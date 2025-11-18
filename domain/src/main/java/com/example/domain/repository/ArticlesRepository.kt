package com.example.domain.repository

import com.example.domain.module.Article
import com.example.domain.module.ArticlesResult
import kotlinx.coroutines.flow.Flow

interface ArticlesRepository {

    suspend fun getArticlesFromServer(): ArticlesResult

    suspend fun getArticlesFromDb(): Flow<List<Article>>

    suspend fun syncArticlesDbWithServer()

    suspend fun getArticleById(articleId: String): Flow<Article>

    fun filterArticles(articles: List<Article>, query: String): List<Article>


}