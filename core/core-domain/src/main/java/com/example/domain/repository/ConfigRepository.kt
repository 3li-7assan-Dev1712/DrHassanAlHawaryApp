package com.example.domain.repository

import com.example.domain.module.AppConfig
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun getAppConfig(): Flow<AppConfig>
}