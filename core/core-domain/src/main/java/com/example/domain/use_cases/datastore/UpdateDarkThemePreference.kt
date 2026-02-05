package com.example.domain.use_cases.datastore


import com.example.domain.repository.DataStoreRepository
import javax.inject.Inject


class UpdateDarkThemePreference @Inject constructor(
    private val dataStoreRepository: DataStoreRepository


) {


    suspend operator fun invoke(isDarkTheme: Boolean) {
        return dataStoreRepository.updateDarkThemePreference(isDarkTheme)

    }


}