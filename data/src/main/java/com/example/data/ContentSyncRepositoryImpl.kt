package com.example.data

import androidx.room.withTransaction
import com.example.data.mappers.toEntity
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_firebase.VideoFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.LocalDataStore
import com.example.domain.repository.ContentSyncRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Delta sync on updatedAt for the four content collections. Only rows already cached are
 * refreshed (or dropped, once soft-deleted): a changed item that isn't cached is left to
 * the list or series screen that pages it in. Inserting it here would put it out of
 * order in the paged lists, whose next page starts after the last cached item.
 */
class ContentSyncRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val audioFirestoreSource: AudioFirestoreSource,
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val videoFirestoreSource: VideoFirestoreSource,
    private val imageFirestoreSource: ImageFirestoreSource,
    private val localDataStore: LocalDataStore
) : ContentSyncRepository {

    private val audioDao = appDatabase.audioDao()
    private val articleDao = appDatabase.articleDao()
    private val videoDao = appDatabase.videoDao()
    private val imageDao = appDatabase.imageDao()

    private val syncMutex = Mutex()

    override suspend fun syncChangedContent() {
        syncMutex.withLock {
            syncAudios()
            syncArticles()
            syncVideos()
            syncImageGroups()
        }
    }

    private suspend fun syncAudios() {
        val changed = audioFirestoreSource.getUpdatedAudios(since(localDataStore.getLastAudioSync()))
        if (changed.isEmpty()) return
        changed.chunked(ID_BATCH).forEach { batch ->
            val cachedIds = audioDao.getCachedIds(batch.map { it.id }).toSet()
            val (deletedItems, activeItems) = batch.filter { it.id in cachedIds }.partition { it.isDeleted }
            appDatabase.withTransaction {
                deletedItems.forEach { audioDao.deleteById(it.id) }
                audioDao.upsertKeepingUserData(activeItems.map { it.toEntity() })
            }
        }
        localDataStore.setLastAudioSync(newest(changed.map { it.updatedAt }))
    }

    private suspend fun syncArticles() {
        val changed = firebaseArticlesSource.getUpdatedArticles(since(localDataStore.getLastArticleSync()))
        if (changed.isEmpty()) return
        changed.chunked(ID_BATCH).forEach { batch ->
            val cachedIds = articleDao.getCachedIds(batch.map { it.id }).toSet()
            val (deletedItems, activeItems) = batch.filter { it.id in cachedIds }.partition { it.isDeleted }
            appDatabase.withTransaction {
                deletedItems.forEach { articleDao.deleteById(it.id) }
                articleDao.upsertAll(activeItems.map { it.toEntity() })
            }
        }
        localDataStore.setLastArticleSync(newest(changed.map { it.updatedAt }))
    }

    private suspend fun syncVideos() {
        val changed = videoFirestoreSource.getUpdatedVideos(since(localDataStore.getLastVideoSync()))
        if (changed.isEmpty()) return
        changed.chunked(ID_BATCH).forEach { batch ->
            val cachedIds = videoDao.getCachedIds(batch.map { it.id }).toSet()
            val (deletedItems, activeItems) = batch.filter { it.id in cachedIds }.partition { it.isDeleted }
            appDatabase.withTransaction {
                deletedItems.forEach { videoDao.deleteById(it.id) }
                videoDao.upsertAll(activeItems.map { it.toEntity() })
            }
        }
        localDataStore.setLastVideoSync(newest(changed.map { it.updatedAt }))
    }

    private suspend fun syncImageGroups() {
        val changed = imageFirestoreSource.getUpdatedImageGroups(since(localDataStore.getLastImageGroupSync()))
        if (changed.isEmpty()) return
        changed.chunked(ID_BATCH).forEach { batch ->
            val cachedIds = imageDao.getCachedIds(batch.map { it.id }).toSet()
            val (deletedItems, activeItems) = batch.filter { it.id in cachedIds }.partition { it.isDeleted }
            appDatabase.withTransaction {
                deletedItems.forEach { imageDao.deleteGroupWithImages(it.id) }
                imageDao.upsertImageGroups(activeItems.map { it.toEntity() })
            }
        }
        localDataStore.setLastImageGroupSync(newest(changed.map { it.updatedAt }))
    }

    /** A collection never synced starts at [FIRST_SYNC_FROM]. */
    private fun since(lastSync: Long): Long = if (lastSync == 0L) FIRST_SYNC_FROM else lastSync

    /** The server's clock, not the device's: the next sync asks for what changed after it. */
    private fun newest(updatedAt: List<Timestamp?>): Long = updatedAt.maxOf { it?.toDate()?.time ?: 0L }

    private companion object {
        /**
         * 2026-10-01 00:00 UTC, before the series backfill bumped updatedAt on every item it
         * placed in a series: devices that cached those items earlier pick up their seriesId.
         * Fresh installs page everything in already, so this only costs them one catch-up read.
         */
        const val FIRST_SYNC_FROM = 1_790_812_800_000L

        /** Keeps each `IN (...)` well under SQLite's 999 bound parameters (Android 7-10). */
        const val ID_BATCH = 500
    }
}
