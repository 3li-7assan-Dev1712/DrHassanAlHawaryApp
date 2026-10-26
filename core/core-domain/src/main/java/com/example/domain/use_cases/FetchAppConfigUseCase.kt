package com.example.domain.use_cases

import com.example.domain.module.AppConfig
import com.example.domain.repository.ConfigRepository
import javax.inject.Inject

/** One server read of the config (the maintenance screen's «إعادة المحاولة»). */
class FetchAppConfigUseCase @Inject constructor(
    private val repository: ConfigRepository
) {
    suspend operator fun invoke(): AppConfig? = repository.fetchAppConfig()
}
