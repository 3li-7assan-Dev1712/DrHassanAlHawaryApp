package com.example.hassanalhawary.domain.repository

import android.util.Log
import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.model.ArticlesResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class ArticlesRepositoryImpl
@Inject constructor(
    private val firestoreDb: FirebaseFirestore
) : ArticlesRepository {
    override suspend fun getAllArticles(): ArticlesResult {

        val allArts: MutableList<Article> = mutableListOf()
        firestoreDb.collection("articles")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    try {
                        Log.d("Ali Has", "fetchAllArticles: Error")
                        val article =
                            Article(
                                id = doc.id,
                                title = doc.get("title").toString(),
                                fullContent = doc.get("content").toString()
                            )
                        allArts.add(article)
                    } catch (e: Exception) {
                        ArticlesResult(errorMessage = e.message)
                    }
                }

            }
            .addOnFailureListener { exception ->
                ArticlesResult(errorMessage = exception.message)
            }
            .addOnCompleteListener {


            }.await()
        return ArticlesResult(allArts)

    }

}