package com.example.data_firebase

import android.util.Log
import com.example.domain.module.Article
import com.example.domain.module.ArticlesResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
 
class FirebaseArticlesSource
@Inject constructor(
    private val firestoreDb: FirebaseFirestore
) {


    /**
     * Uploads a new article to the "articles" collection in Firestore.
     * @param article The Article domain model object to be uploaded.
     */
    suspend fun uploadArticle(article: Article) {
        try {

            val newArticleRef = firestoreDb.collection("articles").document()
            val articleId = newArticleRef.id
            // Create a map of the data to upload from the Article object
            val articleData = hashMapOf(
                "id" to articleId,
                "title" to article.title,
                "content" to article.content,
                "publishDate" to article.publishDate // The Article model already has a Date object
            )

            // Add a new document with a generated ID to the "articles" collection
            firestoreDb.collection("articles")
                .add(articleData)
                .await() // .await() waits for the operation to complete

            Log.d("FirebaseArticlesSource", "Article uploaded successfully: ${article.title}")
        } catch (e: Exception) {
            Log.e("FirebaseArticlesSource", "Error uploading article: ${e.message}", e)
            // Re-throw the exception so the UseCase/ViewModel can catch it and show an error to the user
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
            val articles = snapshot.map { document ->
                Article(
                    id = document.id,
                    title = document.getString("title") ?: "No Title",
                    content = document.getString("content") ?: "No Content",
                    publishDate = document.getDate("publishDate") ?: Date()
                )
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

    suspend fun getArticlesFromFirestore(): ArticlesResult {
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


    suspend fun getArticleById(articleId: String): ArticlesResult {

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


    fun syncArticlesDbWithServer(): Flow<List<Article>> {
        val articlesCollection = firestoreDb.collection("articles")

        // callbackFlow to convert a listener into a Flow.
        return callbackFlow {
            val listenerRegistration = articlesCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // close when error
//                    Os.close(1)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Map Firestore documents to our ArticleEntity
//                    val articles = snapshot.toObjects(ArticleEntity::class.java)
                    Log.d("TAG", "syncArticlesDbWithServer: num : ${snapshot.size()}")
                    val articles = snapshot.map { documentSnapshot ->
                        Article(
                            id = documentSnapshot.id,
                            publishDate = documentSnapshot.getDate("publishDate") ?: Date(),
                            title = documentSnapshot.getString("title") ?: "",
                            content = documentSnapshot.getString("content") ?: ""
                        )
                    }
                    Log.d("", "syncArticlesDbWithServer: num : ${articles.size}")
                    // Send the fresh list of articles through the flow
                    trySend(articles).isSuccess
                }
            }
            // When the flow is cancelled, remove the listener.
            awaitClose { listenerRegistration.remove() }
        }

    }


}