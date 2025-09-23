package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.core.util.NetworkMonitor
import com.example.hassanalhawary.core.util.NetworkStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentNetworkStatusUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {


    operator fun invoke(): Flow<NetworkStatus> =
       networkMonitor.networkStatus

}