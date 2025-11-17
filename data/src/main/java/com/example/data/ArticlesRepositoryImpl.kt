package com.example.data

import android.util.Log
import com.example.data.di.ApplicationScope
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.ArticleDao
import com.example.data_local.model.ArticleEntity
import com.example.data_local.model.toDomainModel
import com.example.domain.module.Article
import com.example.domain.module.ArticlesResult
import com.example.domain.repository.ArticlesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


class ArticlesRepositoryImpl
@Inject constructor(
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val articleDao: ArticleDao,
    @ApplicationScope private val externalScope: CoroutineScope
) : ArticlesRepository {



    init {
        externalScope.launch {
            syncArticlesDbWithServer()
        }
    }

    override suspend fun getArticlesFromServer(): ArticlesResult {
        return firebaseArticlesSource.getArticlesFromFirestore()
    }



    override suspend fun getArticleById(articleId: String): ArticlesResult {

        return firebaseArticlesSource.getArticleById(articleId)

    }


    override fun filterArticles(
        articles: List<Article>, query: String
    ): List<Article> {
        if (query.isBlank()) {
            return articles
        }
        return articles.filter { article ->
            article.title.contains(query, ignoreCase = true) || article.content.contains(
                query,
                ignoreCase = true
            )
        }
    }

//    override suspend fun getLatestArticles(): ArticlesResult {
//        val latestArticles: MutableList<Article> = mutableListOf()
//        return try {
//            val querySnapshot =
//                firestoreDb.collection("articles").orderBy("publishDate").limit(5).get().await()
//            for (document in querySnapshot.documents) {
//                val article = Article(
//                    id = document.id,
//                    title = document.getString("title") ?: "",
//                    content = document.getString("content") ?: "",
//                    publishDate = document.getDate("publishDate") ?: Date()
//                )
//                latestArticles.add(article)
//            }
//            if (latestArticles.isNotEmpty()) {
//                ArticlesResult(latestArticles)
//            } else {
//                ArticlesResult(errorMessage = "No articles found")
//            }
//
//
//        } catch (e: Exception) {
//            ArticlesResult(errorMessage = e.message)
//        }


//    }


    override suspend fun getArticlesFromDb(): Flow<List<Article>> {
        return articleDao.getArticlesFlow().map { entities ->
            Log.d("ArtReopImpl", "getArticlesFromDb: num is : ${entities.size}")
            entities.map { it.toDomainModel() }
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
            // This 'collect' block runs ONLY when the listener sends a new list.
            //  sync the fresh data to our Room database.
            articleDao.syncArticles(articlesFromFirestore)
        }

    }
}