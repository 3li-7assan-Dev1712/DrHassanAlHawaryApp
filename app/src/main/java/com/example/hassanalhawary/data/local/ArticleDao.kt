package com.example.hassanalhawary.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.hassanalhawary.data.local.model.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {


    @Query("SELECT * FROM articles ORDER BY publishDate ASC")
    fun getArticlesFlow(): Flow<List<ArticleEntity>>

    @Upsert
    suspend fun saveArticles(articles: List<ArticleEntity>)


}