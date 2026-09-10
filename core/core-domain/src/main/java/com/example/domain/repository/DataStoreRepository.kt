package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {


    fun observeCompleted(): Flow<Boolean>


    suspend fun setCompleted(completed: Boolean)


    fun isDarkTheme(): Flow<Boolean>

    suspend fun updateDarkThemePreference(isDarkTheme: Boolean)

    fun brandTheme(): Flow<String>

    suspend fun updateBrandThemePreference(brandTheme: String)

    fun getLastSyncTime(): Flow<Long>

    suspend fun updateLastSyncTime(time: Long)

}
