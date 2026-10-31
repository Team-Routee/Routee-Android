package com.routee.android.core.data.connectivity.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay

private const val VALIDATION_CHECK_COUNT = 10
private const val VALIDATION_CHECK_INTERVAL_MILLIS = 200L

internal class ConnectivityRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ConnectivityRepository {
    override suspend fun isConnected(): Boolean = awaitNetworkValidation(::currentNetworkState)

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
