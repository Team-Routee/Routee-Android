package com.routee.android.core.data.location.repository

import com.routee.android.core.data.location.model.DeviceLocation
import kotlinx.coroutines.flow.Flow

/** 기기 위치 */
interface LocationRepository {
    fun observeLocations(): Flow<DeviceLocation>

    /** 기기에 남아 있는 마지막 위치, 없으면 null. 지도 첫 위치용 */
    suspend fun getLastLocation(): Result<DeviceLocation?>
}
