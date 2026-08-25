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
import com.example.domain.use_cases.IsUserLoggedInUseCase

import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
): RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        if (!isUserLoggedInUseCase()) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
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

            // A page can come back entirely soft-deleted (isDeleted=true), which upserts
            // nothing into Room. Deriving the next cursor from the local table's last row
            // (as before) would then never advance past that page, re-fetching the same
            // dead page forever. Instead, loop and advance the cursor from the last document
            // actually fetched from Firestore until we find active items or truly run out.
            while (true) {
                val (articlesFromFirebase, endOfPaginationReached) = firebaseArticlesSource.getArticlesPage(
                    lastPublishDate = currentLastPublishDate,
                    limit = state.config.pageSize.toLong()
                )

                lastResultEndOfPaginationReached = endOfPaginationReached

                if (articlesFromFirebase.isEmpty()) break

                val (deletedItems, activeItems) = articlesFromFirebase.partition { it.isDeleted }

                appDatabase.withTransaction {
                    deletedItems.forEach {
                        appDatabase.articleDao().deleteById(it.id)
                    }

                    if (activeItems.isNotEmpty()) {
                        appDatabase.articleDao().upsertAll(activeItems.map { it.toEntity() })
                    }
                }

                if (activeItems.isNotEmpty() || lastResultEndOfPaginationReached) {
                    break
                }
                currentLastPublishDate = articlesFromFirebase.last().publishDate?.toDate()?.time
            }

            MediatorResult.Success(endOfPaginationReached = lastResultEndOfPaginationReached)

        } catch(e: IOException) {
            MediatorResult.Error(e)
        } catch(e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
