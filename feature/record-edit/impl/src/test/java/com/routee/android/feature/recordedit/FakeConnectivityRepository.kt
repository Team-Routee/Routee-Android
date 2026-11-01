package com.routee.android.feature.recordedit

import com.routee.android.core.data.connectivity.repository.ConnectivityRepository

internal class FakeConnectivityRepository : ConnectivityRepository {
    var connected = true

    override suspend fun isConnected(): Boolean = connected
}
