package com.example.domain.repository

import com.example.domain.module.Article
import kotlinx.coroutines.flow.Flow

interface ArticlesRepository {


    suspend fun getPagingArticlesFromDb(query: String): Flow<List<Article>>

    suspend fun syncArticlesDbWithServer()

    suspend fun getArticleById(articleId: String): Flow<Article>

    fun filterArticles(articles: List<Article>, query: String): List<Article>

    suspend fun getLatestArticlesFromDb(): Flow<List<Article>>




}