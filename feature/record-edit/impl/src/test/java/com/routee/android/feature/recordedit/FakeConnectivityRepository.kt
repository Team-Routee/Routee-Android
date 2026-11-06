package com.routee.android.feature.recordedit

import com.routee.android.core.data.connectivity.repository.ConnectivityRepository
import kotlinx.coroutines.flow.flowOf

internal class FakeConnectivityRepository : ConnectivityRepository {
    var connected = true

    override fun observeConnection() = flowOf(connected)

    override suspend fun isConnected(): Boolean = connected
}
