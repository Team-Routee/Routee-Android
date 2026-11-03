package com.routee.android.core.data.connectivity.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

private const val VALIDATION_CHECK_COUNT = 10
private const val VALIDATION_CHECK_INTERVAL_MILLIS = 200L

internal class ConnectivityRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ConnectivityRepository {
    override suspend fun isConnected(): Boolean = awaitNetworkValidation(::currentNetworkState)

    override fun observeConnection() = callbackFlow {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        if (manager == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            private var currentNetwork: Network? = null

            override fun onAvailable(network: Network) {
                currentNetwork = network
            }

            override fun onLost(network: Network) {
                if (network == currentNetwork) {
                    currentNetwork = null
                    trySend(false)
                }
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                if (network == currentNetwork) {
                    trySend(
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                    )
                }
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        trySend(currentNetworkState() == NetworkState.VALIDATED)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun currentNetworkState(): NetworkState {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            ?: return NetworkState.DISCONNECTED
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return NetworkState.DISCONNECTED
        return when {
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkState.DISCONNECTED
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> NetworkState.VALIDATED
            else -> NetworkState.VALIDATING
        }
    }
}

internal enum class NetworkState { DISCONNECTED, VALIDATING, VALIDATED }

internal suspend fun awaitNetworkValidation(currentState: () -> NetworkState): Boolean {
    repeat(VALIDATION_CHECK_COUNT) {
        when (currentState()) {
            NetworkState.DISCONNECTED -> return false
            NetworkState.VALIDATED -> return true
            NetworkState.VALIDATING -> delay(VALIDATION_CHECK_INTERVAL_MILLIS)
        }
    }
    return currentState() == NetworkState.VALIDATED
}
