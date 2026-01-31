package com.example.study.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


val Context.contentDataStore: DataStore<Preferences> by preferencesDataStore(name = "content_versions")

private const val LEVELS_VERSION_KEY = "levels_version"

@Singleton
class ContentVersionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.contentDataStore

    suspend fun getLevelsVersion(): Long {
        return getVersion(LEVELS_VERSION_KEY) ?: 0L
    }

    suspend fun updateLevelsVersion(version: Long) {
        updateVersion(LEVELS_VERSION_KEY, version)
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

}
