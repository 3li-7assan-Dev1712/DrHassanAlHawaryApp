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

    /** The paged list shows standalone articles only: a series' articles are reached through its card. */
    @Query("""
        SELECT * FROM articles
        WHERE isDeleted = 0 AND seriesId IS NULL AND (:query = '' OR title LIKE '%' || :query || '%')
        ORDER BY publishDate DESC
    """)
    fun getArticlesPagingSource(query: String): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE seriesId = :seriesId AND isDeleted = 0 ORDER BY episodeNumber ASC")
    fun getSeriesArticles(seriesId: String): Flow<List<ArticleEntity>>

    /** Articles cached as part of [seriesId] that the server no longer lists in it become standalone. */
    @Query("UPDATE articles SET seriesId = NULL, episodeNumber = NULL WHERE seriesId = :seriesId AND id NOT IN (:keepIds)")
    suspend fun leaveSeries(seriesId: String, keepIds: List<String>)

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteById(articleId: String)

    @Transaction
    suspend fun syncArticles(articles: List<ArticleEntity>) {
        upsertAll(articles)
    }

    @Query("SELECT * FROM articles WHERE id = :articleId AND isDeleted = 0")
    fun getArticleById(articleId: String): Flow<ArticleEntity?>

    @Query("SELECT * FROM articles WHERE isDeleted = 0 ORDER BY publishDate DESC LIMIT 5")
    fun getLatestArticles(): Flow<List<ArticleEntity>>
}
