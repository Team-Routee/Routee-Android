package com.routee.android.core.data.location.repository

import com.routee.android.core.data.location.model.DeviceLocation
import kotlinx.coroutines.flow.Flow

/** 기기 위치 */
interface LocationRepository {
    fun observeLocations(): Flow<DeviceLocation>
}
