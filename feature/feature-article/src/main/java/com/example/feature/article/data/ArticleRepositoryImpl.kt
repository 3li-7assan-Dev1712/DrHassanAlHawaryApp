package com.example.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.AppDatabase
import com.example.data_local.ArticleDao
import com.example.domain.module.Article
import com.example.feature.article.data.ArticleRemoteMediator
import com.example.feature.article.data.mapper.toDomainModel
import com.example.feature.article.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class ArticleRepositoryImpl
@Inject constructor(
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val appDatabase: AppDatabase,
) : ArticleRepository {

    private val articleDao: ArticleDao = appDatabase.articleDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getPaginatedArticles(query: String): Flow<PagingData<Article>> {

        return Pager(
            config = PagingConfig(
                // Set a page size. This is passed to your RemoteMediator's 'state'.
                pageSize = 5,
                enablePlaceholders = false
            ),
            remoteMediator = ArticleRemoteMediator(
                appDatabase = appDatabase,
                firebaseArticlesSource = firebaseArticlesSource
                // when add search, I will pass the query here
            ),
            // The PagingSourceFactory ALWAYS points to the local database (Room).
            // The RemoteMediator will fill this database for the PagingSource to read.
            pagingSourceFactory = {
                articleDao.getArticlesPagingSource(query)
            }
        ).flow.map { pagingData ->
            // The data from the PagingSource is ArticleEntity, so we map it to the domain model
            pagingData.map { articleEntity ->
                articleEntity.toDomainModel()
            }
        }

    }

}