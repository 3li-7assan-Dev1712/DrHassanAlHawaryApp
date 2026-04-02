package com.example.feature.article.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_local.AppDatabase
import com.example.data_local.model.ArticleEntity
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseArticlesSource: FirebaseArticlesSource
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {
            val initialPublishDate = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    lastItem.publishDate
                }
            }

            var currentLastPublishDate = initialPublishDate
            var lastResultEndOfPaginationReached = false

            // We use a loop to handle the case where a page from the server 
            // contains ONLY deleted items, ensuring we don't return an empty 
            // result to the Pager until we either find active items or reach the end.
            while (true) {
                val (articlesPage, endOfPaginationReached) = firebaseArticlesSource.getArticlesPage(
                    lastPublishDate = currentLastPublishDate,
                    limit = state.config.pageSize.toLong()
                )
                
                lastResultEndOfPaginationReached = endOfPaginationReached

                if (articlesPage.isEmpty()) break

                val (deletedItems, activeItems) = articlesPage.partition { it.isDeleted }

                appDatabase.withTransaction {
                    deletedItems.forEach { dto ->
                        appDatabase.articleDao().deleteById(dto.id)
                    }
                    
                    if (activeItems.isNotEmpty()) {
                        val entities = activeItems.map { dto ->
                            ArticleEntity(
                                id = dto.id,
                                title = dto.title,
                                content = dto.content,
                                publishDate = dto.publishDate?.toDate()?.time ?: 0L,
                                updatedAt = dto.updatedAt?.toDate()?.time ?: 0L,
                                isDeleted = dto.isDeleted,
                                type = dto.type
                            )
                        }
                        appDatabase.articleDao().upsertAll(entities)
                    }
                }

                // If we found some active items to show, or we reached the end of the server data, we stop.
                if (activeItems.isNotEmpty() || endOfPaginationReached) {
                    break
                }

                // Otherwise, move currentLastPublishDate to the last item of the current (all-deleted) page and continue.
                currentLastPublishDate = articlesPage.last().publishDate?.toDate()?.time
            }

            MediatorResult.Success(endOfPaginationReached = lastResultEndOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
