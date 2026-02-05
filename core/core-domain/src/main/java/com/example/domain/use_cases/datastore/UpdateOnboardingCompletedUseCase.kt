package com.example.domain.use_cases.datastore


import com.example.domain.repository.DataStoreRepository
import javax.inject.Inject


class UpdateOnboardingCompletedUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository


) {


    suspend operator fun invoke() {
        return dataStoreRepository.setCompleted(true)

    }


}