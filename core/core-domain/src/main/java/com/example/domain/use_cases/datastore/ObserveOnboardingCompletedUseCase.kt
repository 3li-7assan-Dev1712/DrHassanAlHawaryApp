package com.example.domain.use_cases.datastore


import com.example.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ObserveOnboardingCompletedUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository


) {


    operator fun invoke(): Flow<Boolean> {
        return dataStoreRepository.observeCompleted()

    }


}