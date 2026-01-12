package com.example.feature.article.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ArticleEntity
import com.example.feature.article.data.mapper.toEntity

import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)

class ArticleRemoteMediator @Inject  constructor(
    private val appDatabase: AppDatabase,
    private val firebaseArticlesSource: FirebaseArticlesSource
): RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {

            val lastDocumentId = when (loadType) {
                LoadType.REFRESH -> {
                    // For a refresh, we start from the beginning.
                    null
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // For appending, get the last item loaded from the PagingState.
                    // Its publishDate will be our cursor.
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    // We need the ID of the last item to tell Firestore where to start the next page.
                    lastItem.id
                }
            }

            val (articlesFromFirebase, endOfPaginationReached) = firebaseArticlesSource.getArticles(
                lastDocumentId = lastDocumentId,
                limit = state.config.pageSize.toLong()
            )

            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // Use your actual articleDao to clear the data
                    appDatabase.articleDao().clearAll()
                }

                val articleEntities = articlesFromFirebase.map { it.toEntity() }

                // Use your actual articleDao to insert the new data
                appDatabase.articleDao().upsertAll(articleEntities)
            }

            // The result from firebaseArticlesSource already tells us if the end is reached
            MediatorResult.Success(
                endOfPaginationReached = endOfPaginationReached
            )

        } catch(e: IOException) {
            MediatorResult.Error(e)
        } catch(e: Exception) {
            MediatorResult.Error(e)
        }
    }
}