package com.example.data

import com.example.data.mappers.toDomainModel
import com.example.data.mappers.toEntity
import com.example.data_firebase.SeriesFirestoreSource
import com.example.data_local.AppDatabase
import com.example.data_local.LocalDataStore
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class SeriesRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val seriesFirestoreSource: SeriesFirestoreSource,
    private val localDataStore: LocalDataStore
) : SeriesRepository {

    private val seriesDao = appDatabase.seriesDao()

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
}
