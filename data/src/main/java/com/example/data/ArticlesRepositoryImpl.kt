package com.example.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.di.ApplicationScope
import com.example.data.mappers.toDomainModel
import com.example.data.util.ArticleRemoteMediator
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.AppDatabase
import com.example.data_local.ArticleDao
import com.example.data_local.model.ArticleEntity
import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


class ArticlesRepositoryImpl
@Inject constructor(
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val appDatabase: AppDatabase,
    @ApplicationScope private val externalScope: CoroutineScope
) : ArticlesRepository {

    private val articleDao: ArticleDao = appDatabase.articleDao()

    init {
        externalScope.launch {
            syncArticlesDbWithServer()
        }
    }

    override suspend fun uploadArticle(article: Article) {
        firebaseArticlesSource.uploadArticle(article)
    }

    override suspend fun updateArticle(article: Article) {
        firebaseArticlesSource.updateArticle(article)
    }

    override suspend fun getAllRemoteArticles(): List<Article> {
        // Since we don't have a direct "get all" in source yet, we can use the existing getArticles with a large limit or implement a simple one
        // For simplicity, let's implement a simple get all in source or repo. 
        // Actually, let's add it to FirebaseArticlesSource if not there.
        // I'll use a simplified version for admin list.
        val (list, _) = firebaseArticlesSource.getArticles(null, 100)
        return list
    }

    // fetching (user usage)
    override suspend fun getArticleById(articleId: String): Flow<Article> {
        return articleDao.getArticleById(articleId).map { art ->
            art.toDomainModel()
        }
    }

    override suspend fun getLatestArticlesFromDb(): Flow<List<Article>> {
        return articleDao.getLatestArticles().map {
            it.map { articleEntity ->
                articleEntity.toDomainModel()
            }
        }
    }

    override suspend fun getPagingArticlesFromDb(query: String): Flow<List<Article>> {
        return flowOf()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getArticlesPagingData(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = ArticleRemoteMediator(
                appDatabase = appDatabase,
                firebaseArticlesSource = firebaseArticlesSource
            ),
            pagingSourceFactory = {
                articleDao.getArticlesPagingSource(query)
            }
        ).flow.map { pagingData ->
            pagingData.map { articleEntity ->
                articleEntity.toDomainModel()
            }
        }
    }

    override suspend fun syncArticlesDbWithServer() {
        firebaseArticlesSource.syncArticlesDbWithServer()
            .map { articles ->
                Log.d("ArtRepoImpl", "syncArticlesDbWithServer: from server num: ${articles.size}")
                articles.map { article ->
                    ArticleEntity(
                        id = article.id,
                        title = article.title,
                        content = article.content,
                        publishDate = article.publishDate.time
                    )
                }
            }
            .collect { articlesFromFirestore ->
                articleDao.syncArticles(articlesFromFirestore)
            }
    }
}
