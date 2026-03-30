package com.example.data.util

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ArticleEntity

import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseArticlesSource: FirebaseArticlesSource
): RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {

            val lastPublishDate = when (loadType) {
                LoadType.REFRESH -> {
                    null
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    lastItem.publishDate
                }
            }

            val (articlesFromFirebase, endOfPaginationReached) = firebaseArticlesSource.getArticlesPage(
                lastPublishDate = lastPublishDate,
                limit = state.config.pageSize.toLong()
            )

            appDatabase.withTransaction {
                // Partition results: delete items marked as isDeleted on server, upsert the rest
                val (deletedItems, activeItems) = articlesFromFirebase.partition { it.isDeleted }

                deletedItems.forEach { 
                    appDatabase.articleDao().deleteById(it.id)
                }

                val articleEntities = activeItems.map { it.toEntity() }
                appDatabase.articleDao().upsertAll(articleEntities)
            }

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
