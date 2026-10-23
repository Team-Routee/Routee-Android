package com.routee.android.feature.workout.recording.model

import android.location.Location
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.map.model.MapPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

internal fun DeviceLocation.toLocation(): Location = Location(PROVIDER).also {
    it.latitude = latitude
    it.longitude = longitude
    it.time = time.toEpochMilli()
    accuracyMeters?.let { accuracy -> it.accuracy = accuracy }
}

// 지도 표시용, 마지막 위치부터
internal fun LocationRepository.observeLocationsFromLast(): Flow<DeviceLocation> =
    observeLocations().onStart { getLastLocation().getOrNull()?.let { emit(it) } }

internal fun DeviceLocation.toMapPoint(): MapPoint = MapPoint(latitude, longitude)

// 내 위치에서 시작할 때 줌
internal const val MY_LOCATION_ZOOM = 16.0

private const val PROVIDER = "routee"
