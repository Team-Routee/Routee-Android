package com.routee.android.core.data.location.local

import com.routee.android.core.data.location.model.DeviceLocation
import kotlinx.coroutines.flow.Flow

internal interface LocationDataSource {
    fun locations(): Flow<DeviceLocation>

    suspend fun lastLocation(): DeviceLocation?
}
