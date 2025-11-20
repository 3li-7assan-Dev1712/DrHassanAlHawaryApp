package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.data_local.model.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {


    @Query("""
        SELECT * FROM articles
        WHERE :query = '' OR title LIKE '%' || :query || '%'
        ORDER BY publishDate DESC
    """)
    fun getArticlesPagingSource(query: String): PagingSource<Int, ArticleEntity>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()

    @Transaction
    suspend fun syncArticles(articles: List<ArticleEntity>) {
        clearAll()
        upsertAll(articles)
    }

    @Query("SELECT * FROM articles WHERE id = :articleId ")
    fun getArticleById(articleId: String): Flow<ArticleEntity>

    @Query("SELECT * FROM articles ORDER BY publishDate DESC LIMIT 5")
    fun getLatestArticles(): Flow<List<ArticleEntity>>
}