package com.example.domain.use_cases.datastore


import com.example.domain.repository.DataStoreRepository
import javax.inject.Inject


class UpdateBrandThemePreference @Inject constructor(
    private val dataStoreRepository: DataStoreRepository


) {


    suspend operator fun invoke(brandTheme: String) {
        return dataStoreRepository.updateBrandThemePreference(brandTheme)

    }


}
