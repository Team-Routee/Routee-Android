package com.routee.android.core.data.location.repository

import com.routee.android.core.data.location.local.LocationDataSource
import com.routee.android.core.data.location.model.DeviceLocation
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource,
) : LocationRepository {
    override fun observeLocations(): Flow<DeviceLocation> = locationDataSource.locations()
}
