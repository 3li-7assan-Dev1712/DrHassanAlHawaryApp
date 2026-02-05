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
}