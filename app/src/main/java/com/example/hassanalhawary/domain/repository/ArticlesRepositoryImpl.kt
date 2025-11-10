package com.example.hassanalhawary.domain.repository

import android.util.Log
import com.example.hassanalhawary.data.local.ArticleDao
import com.example.hassanalhawary.data.local.model.toDomainModel
import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.model.ArticlesResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject


class ArticlesRepositoryImpl
@Inject constructor(
    private val firestoreDb: FirebaseFirestore,
    private val articleDao: ArticleDao
) : ArticlesRepository {
    override suspend fun getAllArticles(): ArticlesResult {

        val allArts: MutableList<Article> = mutableListOf()
        firestoreDb.collection("articles").get().addOnSuccessListener { result ->
            for (doc in result) {
                try {
                    Log.d("Ali Has", "fetchAllArticles: Error")
                    val article = Article(
                        id = doc.id,
                        title = doc.get("title").toString(),
                        content = doc.get("content").toString()
                    )
                    allArts.add(article)
                } catch (e: Exception) {
                    ArticlesResult(errorMessage = e.message)
                }
            }

        }.addOnFailureListener { exception ->
            ArticlesResult(errorMessage = exception.message)
        }.addOnCompleteListener {


        }.await()
        return ArticlesResult(allArts)

    }

    override suspend fun getArticleById(articleId: String): ArticlesResult {

        return try {

            Log.d("ArticlesRepository", "getArticleById: $articleId")
            val documentSnapshot =
                firestoreDb.collection("articles").document(articleId).get().await()
            if (documentSnapshot.exists()) {
                val article = Article(
                    id = documentSnapshot.id,
                    title = documentSnapshot.getString("title") ?: "wwwway",
                    content = documentSnapshot.getString("content") ?: "waaaaaay",
                    publishDate = documentSnapshot.getDate("publishDate") ?: Date()
                )
                ArticlesResult(article = article) // Success, return the article

            } else {
                Log.d("ArticlesRepository", "getArticleById: dose not exists")
                return ArticlesResult(errorMessage = "Article not found")
            }
        } catch (e: Exception) {
            Log.d("ArticlesRepository", "getArticleById: ${e.message}")
            ArticlesResult(errorMessage = e.message)

        }

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

    override suspend fun getLatestArticles(): ArticlesResult {
        val latestArticles: MutableList<Article> = mutableListOf()
        return try {
            val querySnapshot =
                firestoreDb.collection("articles").orderBy("publishDate").limit(5).get().await()
            for (document in querySnapshot.documents) {
                val article = Article(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    content = document.getString("content") ?: "",
                    publishDate = document.getDate("publishDate") ?: Date()
                )
                latestArticles.add(article)
            }
            if (latestArticles.isNotEmpty()) {
                ArticlesResult(latestArticles)
            } else {
                ArticlesResult(errorMessage = "No articles found")
            }



        } catch (e: Exception) {
            ArticlesResult(errorMessage = e.message)
        }


    }


    override suspend fun getArticlesFromDb(): Flow<List<Article>> {
        return articleDao.getArticlesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun syncArticlesDbWithServer() {
//        val querySnapshot =
//            firestoreDb.collection("articles").orderBy("publishDate").get().await()
//        for (document in querySnapshot.documents) {
//            val article = Article(
//                id = document.id,
//                title = document.getString("title") ?: "",
//                content = document.getString("content") ?: "",
//                publishDate = document.getDate("publishDate") ?: Date()
//            )
//            latestArticles.add(article)
//        }
    }
}