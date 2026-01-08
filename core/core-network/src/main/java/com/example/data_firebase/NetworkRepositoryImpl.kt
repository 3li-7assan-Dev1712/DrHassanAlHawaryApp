package com.example.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.domain.module.NetworkStatus
import com.example.domain.repository.NetworkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject


class NetworkRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    NetworkRepository {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    override fun getNetworkStatus(): Flow<NetworkStatus> = callbackFlow @androidx.annotation.RequiresPermission(
        android.Manifest.permission.ACCESS_NETWORK_STATE
    ) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }
        }


        val currentNetwork = connectivityManager.activeNetwork
        if (currentNetwork == null) {
            trySend(NetworkStatus.Unavailable)
        } else {
            val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
            if (capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ) {
                trySend(NetworkStatus.Available)
            } else if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                // Connected to a network, but internet access isn't validated (e.g., captive portal)
                trySend(NetworkStatus.Available)
            }
            else {
                trySend(NetworkStatus.Unavailable)
            }
        }


        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // We are interested in internet connectivity
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI) // for WIFI
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR) // for cellular network
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)


        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

}