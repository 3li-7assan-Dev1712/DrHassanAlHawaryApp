package com.example.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.data.di.ApplicationScope
import com.example.data.mappers.toDomainModel
import com.example.data.util.ArticleRemoteMediator
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.AppDatabase
import com.example.data_local.ArticleDao
import com.example.data_local.model.ArticleEntity
import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
class ArticlesRepositoryImpl
@Inject constructor(
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val appDatabase: AppDatabase,
    private val authRepository: AuthRepository,
    private val articleRemoteMediator: ArticleRemoteMediator,
    @ApplicationScope private val externalScope: CoroutineScope
) : ArticlesRepository {

    private val articleDao: ArticleDao = appDatabase.articleDao()

    init {
        externalScope.launch {
            authRepository.observeAuthState().collectLatest { isLoggedIn ->
                if (isLoggedIn) {
                    syncArticlesDbWithServer()
                }
            }
        }
    }

    override suspend fun uploadArticle(article: Article) {
        firebaseArticlesSource.uploadArticle(article)
    }

    override suspend fun updateArticle(article: Article) {
        firebaseArticlesSource.updateArticle(article)
    }

    override suspend fun getArticleById(articleId: String): Flow<Article?> {
        return articleDao.getArticleById(articleId).map { art ->
            art?.toDomainModel()
        }
    }

    override suspend fun deleteArticle(articleId: String) {
        firebaseArticlesSource.deleteArticle(articleId)
    }

    override suspend fun getAllRemoteArticles(): List<Article> {
        val (list, _) = firebaseArticlesSource.getArticlesPage(null, 100)
        return list.filter { !it.isDeleted }.map { it.toDomainModel() }
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
            remoteMediator = articleRemoteMediator,
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
            .catch { e -> Log.e("ArtRepoImpl", "syncArticlesDbWithServer error: ${e.message}") }
            .collect { articles ->
                Log.d("ArtRepoImpl", "syncArticlesDbWithServer: received ${articles.size} articles")
                
                appDatabase.withTransaction {
                    val (deleted, active) = articles.partition { it.isDeleted }

                    // Physically remove deleted items
                    deleted.forEach { article ->
                        articleDao.deleteById(article.id)
                    }

                    // Map and upsert active ones
                    val activeEntities = active.map { article ->
                        ArticleEntity(
                            id = article.id,
                            title = article.title,
                            content = article.content,
                            publishDate = article.publishDate.time,
                            updatedAt = article.updatedAt,
                            isDeleted = article.isDeleted,
                            type = article.type
                        )
                    }
                    articleDao.upsertAll(activeEntities)
                }
            }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPaginatedArticles(query: String, limit: Int): Flow<PagingData<Article>> {

        return Pager(
            config = PagingConfig(
                // Set a page size. This is passed to your RemoteMediator's 'state'.
                pageSize = limit,
                enablePlaceholders = false
            ),
            remoteMediator = articleRemoteMediator,
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
