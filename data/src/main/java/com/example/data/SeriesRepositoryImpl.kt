package com.example.data

import androidx.room.withTransaction
import com.example.data.mappers.toDomain
import com.example.data.mappers.toDomainModel
import com.example.data.mappers.toEntity
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_firebase.SeriesFirestoreSource
import com.example.data_firebase.VideoFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.LocalDataStore
import com.example.domain.module.Article
import com.example.domain.module.Audio
import com.example.domain.module.ImageGroup
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.module.Video
import com.example.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class SeriesRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val seriesFirestoreSource: SeriesFirestoreSource,
    private val audioFirestoreSource: AudioFirestoreSource,
    private val firebaseArticlesSource: FirebaseArticlesSource,
    private val videoFirestoreSource: VideoFirestoreSource,
    private val imageFirestoreSource: ImageFirestoreSource,
    private val localDataStore: LocalDataStore
) : SeriesRepository {

    private val seriesDao = appDatabase.seriesDao()
    private val audioDao = appDatabase.audioDao()
    private val articleDao = appDatabase.articleDao()
    private val videoDao = appDatabase.videoDao()
    private val imageDao = appDatabase.imageDao()

    // Several screens ask for a sync as they open; one at a time is enough.
    private val syncMutex = Mutex()

    override fun getSeries(collection: SeriesCollection, categoryId: String?): Flow<List<Series>> {
        return seriesDao.getSeries(collection.firestoreName, categoryId).map { entities ->
            entities.mapNotNull { it.toDomainModel() }
        }
    }

    override fun getSeriesById(seriesId: String): Flow<Series?> {
        return seriesDao.getSeriesById(seriesId).map { it?.toDomainModel() }
    }

    override suspend fun syncSeries() {
        syncMutex.withLock {
            val changed = seriesFirestoreSource.getUpdatedSeries(localDataStore.getLastSeriesSync())
            if (changed.isEmpty()) return

            // Deleted series are stored too (isDeleted = 1): the DAO's queries skip them.
            seriesDao.upsertAll(changed.map { it.toEntity() })
            // The server's clock, not the device's: the next sync asks for what changed after it.
            localDataStore.setLastSeriesSync(changed.maxOf { it.updatedAt?.toDate()?.time ?: 0L })
        }
    }

    override fun getSeriesAudios(seriesId: String): Flow<List<Audio>> {
        return audioDao.getSeriesAudios(seriesId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSeriesArticles(seriesId: String): Flow<List<Article>> {
        return articleDao.getSeriesArticles(seriesId).map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getSeriesVideos(seriesId: String): Flow<List<Video>> {
        return videoDao.getSeriesVideos(seriesId).map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getSeriesImageGroups(seriesId: String): Flow<List<ImageGroup>> {
        return imageDao.getSeriesImageGroups(seriesId).map { entities -> entities.map { it.toDomainModel() } }
    }

    // Same isDeleted handling as the mediators: soft-deleted items leave the cache. An item
    // the server no longer lists in the series, but still cached in it, becomes standalone.
    override suspend fun refreshSeriesItems(series: Series): Boolean {
        when (series.collection) {
            SeriesCollection.AUDIOS -> {
                val audios = audioFirestoreSource.fetchSeriesAudios(series.id) ?: return false
                val (deletedItems, activeItems) = audios.partition { it.isDeleted }
                appDatabase.withTransaction {
                    deletedItems.forEach { audioDao.deleteById(it.id) }
                    audioDao.upsertKeepingUserData(activeItems.map { it.toEntity() })
                    audioDao.leaveSeries(series.id, keepIds = activeItems.map { it.id })
                }
            }

            SeriesCollection.ARTICLES -> {
                val articles = firebaseArticlesSource.fetchSeriesArticles(series.id) ?: return false
                val (deletedItems, activeItems) = articles.partition { it.isDeleted }
                appDatabase.withTransaction {
                    deletedItems.forEach { articleDao.deleteById(it.id) }
                    articleDao.upsertAll(activeItems.map { it.toEntity() })
                    articleDao.leaveSeries(series.id, keepIds = activeItems.map { it.id })
                }
            }

            SeriesCollection.VIDEOS -> {
                val videos = videoFirestoreSource.fetchSeriesVideos(series.id) ?: return false
                val (deletedItems, activeItems) = videos.partition { it.isDeleted }
                appDatabase.withTransaction {
                    deletedItems.forEach { videoDao.deleteById(it.id) }
                    videoDao.upsertAll(activeItems.map { it.toEntity() })
                    videoDao.leaveSeries(series.id, keepIds = activeItems.map { it.id })
                }
            }

            SeriesCollection.IMAGE_GROUPS -> {
                val groups = imageFirestoreSource.fetchSeriesImageGroups(series.id) ?: return false
                val (deletedItems, activeItems) = groups.partition { it.isDeleted }
                appDatabase.withTransaction {
                    deletedItems.forEach { imageDao.deleteGroupWithImages(it.id) }
                    imageDao.upsertImageGroups(activeItems.map { it.toEntity() })
                    imageDao.leaveSeries(series.id, keepIds = activeItems.map { it.id })
                }
            }
        }
        return true
    }

    override suspend fun createSeries(series: Series): Result<Unit> {
        return seriesFirestoreSource.createSeries(
            title = series.title,
            collection = series.collection.firestoreName,
            categoryId = series.categoryId,
            hashtag = series.hashtag,
            order = series.order,
        ).onSuccess { syncSeries() }
    }

    override suspend fun updateSeries(series: Series): Result<Unit> {
        return seriesFirestoreSource.updateSeries(
            series.id,
            mapOf(
                "title" to series.title,
                "categoryId" to series.categoryId,
                "hashtag" to series.hashtag,
                "order" to series.order,
            ),
        ).onSuccess { syncSeries() }
    }

    override suspend fun deleteSeries(seriesId: String): Result<Unit> {
        return seriesFirestoreSource.deleteSeries(seriesId).onSuccess {
            // Hidden at once, whether or not the delete reached the delta sync yet.
            seriesDao.markDeleted(seriesId)
            syncSeries()
        }
    }
}
