package com.example.data_firebase

import android.util.Log
import com.example.data_firebase.model.ArticleDto
import com.example.domain.module.Article
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseArticlesSource
@Inject constructor(
    private val firestoreDb: FirebaseFirestore
) {


    private val articlesCollection = firestoreDb.collection("articles")

    /**
     * Uploads a new article to the "articles" collection in Firestore.
     * @param article The Article domain model object to be uploaded.
     */
    suspend fun uploadArticle(article: Article) {
        try {

            val newArticleRef = articlesCollection.document()
            val articleId = newArticleRef.id
            // Create a map of the data to upload from the Article object
            val articleDto = ArticleDto(
                id = articleId,
                title = article.title,
                content = article.content,
                publishDate = article.publishDate
            )

            // Add a new document with a generated ID to the "articles" collection
            newArticleRef.set(articleDto).await()

            Log.d("FirebaseArticlesSource", "Article uploaded successfully: ${article.title}")
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error uploading article: ${e.message}", e)
            // Re-throw the exception so the UseCase/ViewModel can catch it and show an error to the user
            throw e
        }
    }

    suspend fun updateArticle(article: Article) {
        try {
            val articleDto = ArticleDto(
                id = article.id,
                title = article.title,
                content = article.content,
                publishDate = article.publishDate
            )
            articlesCollection.document(article.id).set(articleDto).await()
            Log.d("FirebaseArticlesSource", "Article updated successfully: ${article.title}")
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error updating article: ${e.message}", e)
            throw e
        }
    }


    suspend fun getArticles(lastDocumentId: String?, limit: Long): Pair<List<Article>, Boolean> {
        try {
            // dealy to test the loading
//            delay(3000L)
            Log.d("Ali ", "getArticles: network call")
            // Base query to the "articles" collection, ordered by publishDate descending
            val query = firestoreDb.collection("articles")
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(limit)

            // If lastDocumentId is not null, find that document to use it as a cursor
            val finalQuery = if (lastDocumentId != null) {
                val lastDocumentSnapshot =
                    firestoreDb.collection("articles").document(lastDocumentId).get().await()
                query.startAfter(lastDocumentSnapshot)
            } else {
                query
            }

            // Execute the query
            val snapshot: QuerySnapshot = finalQuery.get().await()
            val articles = snapshot.documents.mapNotNull { document ->
                try {
                    // Step 1: Convert Firestore Document to DTO
                    val dto = document.toObject<ArticleDto>()
                    // Step 2: Convert DTO to Domain Model (Article)
                    dto?.let {
                        Article(
                            id = document.id, // Always use the document's actual ID
                            title = it.title,
                            content = it.content,
                            publishDate = it.publishDate
                        )
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseArticlesSource", "Failed to parse document ${document.id}", e)
                    null // Skip corrupted documents
                }
            }
            // The end of pagination is reached if the number of fetched articles is less than the requested limit.
            val endOfPaginationReached = articles.size < limit
            Log.d(
                "FirebaseArticlesSource",
                "Fetched ${articles.size} articles. End reached: $endOfPaginationReached"
            )

            return Pair(articles, endOfPaginationReached)

        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error fetching articles: ${e.message}", e)
            // In case of an error, return an empty list and assume the end is reached to prevent further loads.
            return Pair(emptyList(), true)
        }
    }


    fun syncArticlesDbWithServer(): Flow<List<Article>> {
        return callbackFlow {
            val listenerRegistration = articlesCollection
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val articles = snapshot.documents.mapNotNull { document ->
                            try {
                                val dto = document.toObject<ArticleDto>()
                                dto?.let {
                                    Article(
                                        id = document.id, // Use the real document ID
                                        title = it.title,
                                        content = it.content,
                                        publishDate = it.publishDate
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "FirebaseArticlesSource",
                                    "Failed to parse document during sync: ${document.id}",
                                    e
                                )
                                null
                            }
                        }
                        trySend(articles)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }


}
