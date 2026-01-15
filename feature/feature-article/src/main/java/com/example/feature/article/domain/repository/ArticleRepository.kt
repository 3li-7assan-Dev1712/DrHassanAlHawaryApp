package com.example.feature.article.domain.repository

import androidx.paging.PagingData
import com.example.domain.module.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {



    fun getPaginatedArticles(query: String, limit: Int = 15): Flow<PagingData<Article>>


}