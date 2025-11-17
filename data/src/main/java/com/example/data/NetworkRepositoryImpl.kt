package com.example.data

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


/*
The class below is helper class to monitor the connectivity status of Dr. Hassan app to realtime check
the network availability and update the UI accordingly.

the goal is whenever user's connectivity changes we want to show a message to the user.

 */


class NetworkRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    NetworkRepository {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /*val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {

                val isInternetActuallyAvailable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (isInternetActuallyAvailable) {
                    trySend(NetworkStatus.Available)
                } else {
                     trySend(NetworkStatus.Unavailable)
                }
            }
        }
        // Check initial state
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


        // Build a request for all networks
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // We are interested in internet connectivity
             .addTransportType(NetworkCapabilities.TRANSPORT_WIFI) // for WIFI
             .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR) // for cellular network
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Unregister callback when the flow is cancelled
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Only emit when the status actually changes
*/
    override fun getNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }
        }

        // check initial state

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


        // Build a request for all networks
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