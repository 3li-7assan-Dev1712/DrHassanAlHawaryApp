package com.example.domain.use_cases

import com.example.domain.module.AppConfig
import com.example.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppConfigUseCase @Inject constructor(
    private val repository: ConfigRepository
) {
    operator fun invoke(): Flow<AppConfig> = repository.getAppConfig()
}