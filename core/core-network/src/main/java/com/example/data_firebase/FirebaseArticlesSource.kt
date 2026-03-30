package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.ArticleDto
import com.example.domain.module.Article
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FirebaseArticlesSource @Inject constructor(
    private val firestoreDb: FirebaseFirestore
) {

    private val articlesCollection = firestoreDb.collection("articles")

    suspend fun uploadArticle(article: Article) {
        try {
            val newArticleRef = articlesCollection.document()
            val articleId = newArticleRef.id
            val now = Timestamp.now()

            val articleDto = ArticleDto(
                id = articleId,
                title = article.title,
                content = article.content,
                publishDate = now,
                updatedAt = now,
                isDeleted = false
            )

            newArticleRef.set(articleDto).await()
            Log.d("FirebaseArticlesSource", "Article uploaded successfully: ${article.title}")
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error uploading article: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateArticle(article: Article) {
        try {
            val updates = mapOf(
                "title" to article.title,
                "content" to article.content,
                "updatedAt" to Timestamp.now()
            )
            articlesCollection.document(article.id).update(updates).await()
            Log.d("FirebaseArticlesSource", "Article updated successfully: ${article.title}")
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error updating article: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteArticle(articleId: String) {
        try {
            articlesCollection.document(articleId).update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Log.d("FirebaseArticlesSource", "Article soft-deleted successfully: $articleId")
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error deleting article: ${e.message}", e)
            throw e
        }
    }

    suspend fun getArticlesPage(
        lastPublishDate: Long?,
        limit: Long
    ): Pair<List<ArticleDto>, Boolean> {
        try {
            // We don't filter by isDeleted here so that the local database 
            // can receive the update if an article is deleted.
            var query = articlesCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastPublishDate != null) {
                query = query.startAfter(Timestamp(Date(lastPublishDate)))
            }

            val snapshot: QuerySnapshot = query.get().await()
            val articles = snapshot.documents.mapNotNull { it.toObject<ArticleDto>() }

            val endOfPaginationReached = articles.size < limit
            return Pair(articles, endOfPaginationReached)
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error fetching articles page: ${e.message}", e)
            return Pair(emptyList(), true)
        }
    }

    suspend fun getUpdatedArticles(lastSyncTime: Long): List<ArticleDto> {
        return try {
            val snapshot = articlesCollection
                .whereGreaterThan("updatedAt", Timestamp(Date(lastSyncTime)))
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<ArticleDto>() }
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error fetching updated articles: ${e.message}", e)
            emptyList()
        }
    }

    fun syncArticlesDbWithServer(): Flow<List<Article>> {
        return callbackFlow {
            // We listen to all articles (including isDeleted) so the local DB stays in sync.
            // Room queries will filter out isDeleted = 1 from the UI.
            val listenerRegistration = articlesCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseArticlesSource", "Listen failed: ${error.message}", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val articles = snapshot.documents.mapNotNull { document ->
                            val dto = document.toObject<ArticleDto>()
                            dto?.let {
                                Article(
                                    id = it.id,
                                    title = it.title,
                                    content = it.content,
                                    publishDate = it.publishDate?.toDate() ?: Date(),
                                    updatedAt = it.updatedAt?.toDate()?.time ?: 0L,
                                    isDeleted = it.isDeleted
                                )
                            }
                        }
                        trySend(articles)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }
}
