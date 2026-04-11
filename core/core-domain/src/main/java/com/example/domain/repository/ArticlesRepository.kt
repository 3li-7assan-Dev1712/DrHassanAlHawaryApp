package com.example.domain.repository

import androidx.paging.PagingData
import com.example.domain.module.Article
import kotlinx.coroutines.flow.Flow

interface ArticlesRepository {


    suspend fun getPagingArticlesFromDb(query: String): Flow<List<Article>>

    suspend fun syncArticlesDbWithServer()

    suspend fun getArticleById(articleId: String): Flow<Article?>

    suspend fun getLatestArticlesFromDb(): Flow<List<Article>>
    suspend fun uploadArticle(article: Article)
    suspend fun updateArticle(article: Article)
    suspend fun deleteArticle(articleId: String)
    suspend fun getAllRemoteArticles(): List<Article>


    fun getPaginatedArticles(query: String, limit: Int = 15): Flow<PagingData<Article>>
}
