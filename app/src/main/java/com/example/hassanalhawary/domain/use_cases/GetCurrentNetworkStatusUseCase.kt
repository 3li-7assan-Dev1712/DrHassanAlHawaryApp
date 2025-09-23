package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.core.util.NetworkMonitor
import com.example.hassanalhawary.core.util.NetworkStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetCurrentNetworkStatusUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {


    suspend operator fun invoke(): NetworkStatus =
       networkMonitor.networkStatus.first()

}