package com.example.feature.home.domain.use_cases

import android.util.Log
import com.example.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class SyncLatestDataUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke() {
        try {
            // It calls the repository methods that handle fetching AND saving.
            homeRepository.syncLatestArticles(limit = 5)
            homeRepository.syncLatestAudios(limit = 5)
            homeRepository.syncLatestImageGroup()
            Log.d("SyncLatestData", "Sync completed successfully.")
        } catch (e: Exception) {
            // The use case can handle errors, like logging.
            // It should not crash the app. The UI will just show old data.
            Log.e("SyncLatestData", "Error syncing latest data: ${e.message}")
        }
    }
}