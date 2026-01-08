package com.example.domain.use_cases


import com.example.domain.module.NetworkStatus
import com.example.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentNetworkStatusUseCase @Inject constructor(
    private val NetworkRepository: NetworkRepository
) {

    operator fun invoke(): Flow<NetworkStatus> {
        return NetworkRepository.getNetworkStatus()
    }


}