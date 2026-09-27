package com.example.data

import com.example.data_local.LocalDataStore
import com.example.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val localDataStore: LocalDataStore
) : DataStoreRepository {

    override fun observeCompleted(): Flow<Boolean> =
        localDataStore.observeCompleted()


    override suspend fun setCompleted(completed: Boolean) {
        localDataStore.setCompleted(completed)
    }

    override fun isDarkTheme(): Flow<Boolean> {
        return localDataStore.isDarkTheme
    }

    override suspend fun updateDarkThemePreference(isDarkTheme: Boolean) {
        localDataStore.setDarkTheme(isDarkTheme)
    }

    override fun brandTheme(): Flow<String> {
        return localDataStore.brandTheme
    }

    override suspend fun updateBrandThemePreference(brandTheme: String) {
        localDataStore.setBrandTheme(brandTheme)
    }

    override fun getLastSyncTime(): Flow<Long> {
        return localDataStore.getLastSyncTime()
    }

    override suspend fun updateLastSyncTime(time: Long) {
        localDataStore.updateLastSyncTime(time)
    }

    override fun readerFontStep(): Flow<Int> = localDataStore.readerFontStep

    override suspend fun setReaderFontStep(step: Int) = localDataStore.setReaderFontStep(step)

    override fun recentSearches(): Flow<List<String>> = localDataStore.recentSearches

    override suspend fun setRecentSearches(queries: List<String>) = localDataStore.setRecentSearches(queries)

    override fun followSystemTheme(): Flow<Boolean> = localDataStore.followSystemTheme

    override suspend fun setFollowSystemTheme(follow: Boolean) = localDataStore.setFollowSystemTheme(follow)
}
