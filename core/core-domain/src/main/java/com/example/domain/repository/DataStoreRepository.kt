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

    /** Article reader text-size step (0..3, 1 = the original size). */
    fun readerFontStep(): Flow<Int>

    suspend fun setReaderFontStep(step: Int)

    /** Recent search queries, newest first. */
    fun recentSearches(): Flow<List<String>>

    suspend fun setRecentSearches(queries: List<String>)

    /** True when the app follows the phone's light/dark setting ("تلقائي"). */
    fun followSystemTheme(): Flow<Boolean>

    suspend fun setFollowSystemTheme(follow: Boolean)

    /** Audio player speed (1.0 = normal). */
    fun playbackSpeed(): Flow<Float>

    suspend fun setPlaybackSpeed(speed: Float)

}
