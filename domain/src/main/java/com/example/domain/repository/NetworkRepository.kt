package com.example.domain.repository

import com.example.domain.module.NetworkStatus
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for monitoring network connectivity.
 * The domain layer uses this interface, without knowing the implementation details.
 */
interface NetworkRepository {
    fun getNetworkStatus(): Flow<NetworkStatus>
}