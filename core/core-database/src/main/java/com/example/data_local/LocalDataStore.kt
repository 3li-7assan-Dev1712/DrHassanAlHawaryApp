package com.example.data_local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


val Context.contentDataStore: DataStore<Preferences> by preferencesDataStore(name = "local_data")

private const val LEVELS_VERSION_KEY = "levels_version"
private const val LAST_PLAYLIST_SYNC = "last_playlist_sync"

private const val LAST_LESSON_SYNC = "last_lesson_sync"

val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")
val KEY_DARK_THEME = booleanPreferencesKey("dark_theme_enabled")


@Singleton
class LocalDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val TAG = "LocalDataStore"
    private val dataStore = context.contentDataStore

    suspend fun getLevelsVersion(): Long {
        return getVersion(LEVELS_VERSION_KEY) ?: 0L
    }

    suspend fun updateLevelsVersion(version: Long) {
        updateVersion(LEVELS_VERSION_KEY, version)
    }

    suspend fun getLastPlaylistSync(): Long {
        return dataStore.data.map {
            it[longPreferencesKey(LAST_PLAYLIST_SYNC)] ?: 0
        }.firstOrNull() ?: 0

    }

    suspend fun setLastPlaylistSync(timestamp: Long) {
        dataStore.edit {
            it[longPreferencesKey(LAST_PLAYLIST_SYNC)] = timestamp
        }
    }


    suspend fun getLastLessonSync(): Long {
        return dataStore.data.map {
            it[longPreferencesKey(LAST_LESSON_SYNC)] ?: 0
        }.firstOrNull() ?: 0

    }

    suspend fun setLastLessonSync(timestamp: Long) {
        dataStore.edit {
            it[longPreferencesKey(LAST_LESSON_SYNC)] = timestamp
        }
    }

    fun observeCompleted(): Flow<Boolean> {
        return context.contentDataStore.data.map { prefs ->
            prefs[KEY_COMPLETED] ?: false
        }
    }

    suspend fun setCompleted(completed: Boolean) {
        context.contentDataStore.edit { prefs ->
            prefs[KEY_COMPLETED] = completed
        }
    }


    suspend fun updateVersion(key: String, version: Long) {
        val prefKey = longPreferencesKey(key)
        dataStore.edit {
            it[prefKey] = version
        }
    }

    suspend fun getVersion(key: String): Long? {
        val prefKey = longPreferencesKey(key)
        return dataStore.data.map { it[prefKey] }.firstOrNull()
    }


    // Theme preferences
    val isDarkTheme: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_DARK_THEME] ?: false
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        Log.d(TAG, "setDarkTheme: $enabled")
        dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

}
