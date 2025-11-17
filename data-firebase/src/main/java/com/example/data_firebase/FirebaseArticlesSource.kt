package com.example.data_firebase

import android.util.Log
import com.example.domain.module.Article
import com.example.domain.module.ArticlesResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseArticlesSource
    constructor(
        private val firestoreDb: FirebaseFirestore
    ){
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