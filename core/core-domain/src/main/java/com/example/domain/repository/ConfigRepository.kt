package com.example.domain.repository

import com.example.domain.module.AppConfig
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun getAppConfig(): Flow<AppConfig>

    /** Reads the config from the server once (bypassing the cache); null when it can't. */
    suspend fun fetchAppConfig(): AppConfig?
}
