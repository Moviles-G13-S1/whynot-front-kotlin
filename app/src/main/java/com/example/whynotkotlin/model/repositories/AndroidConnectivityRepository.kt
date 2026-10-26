package com.example.whynotkotlin.model.repositories

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Adapts Android's [ConnectivityManager] callbacks to [ConnectivityRepository].
 *
 * Only a network that reports both INTERNET and VALIDATED counts as online, so
 * airplane mode or a Wi-Fi without internet access reads as offline. One
 * callback serves the whole app because the state is shared on [scope].
 */
class AndroidConnectivityRepository(
    context: Context,
    scope: CoroutineScope
) : ConnectivityRepository {

    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    override val isOnline: StateFlow<Boolean> =
        callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(currentlyOnline())
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    trySend(capabilities.hasValidatedInternet())
                }

                override fun onLost(network: Network) {
                    trySend(false)
                }
            }

            trySend(currentlyOnline())
            connectivityManager.registerDefaultNetworkCallback(callback)

            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), currentlyOnline())

    private fun currentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasValidatedInternet()
    }

    private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
