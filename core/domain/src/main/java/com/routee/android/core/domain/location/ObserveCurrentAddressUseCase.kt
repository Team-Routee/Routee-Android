package com.routee.android.core.domain.location

import com.routee.android.core.data.geocoding.repository.ReverseGeocodingRepository
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.model.distanceTo
import com.routee.android.core.data.location.repository.LocationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** 현재 위치 주소. 마지막으로 주소를 받은 곳에서 50m 넘게 움직였을 때만 다시 조회하고, 실패하면 다음 위치에서 다시 시도 */
class ObserveCurrentAddressUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val reverseGeocodingRepository: ReverseGeocodingRepository,
) {
    operator fun invoke(): Flow<String> = flow {
        var addressedAt: DeviceLocation? = null
        locationRepository.observeLocations().collect { location ->
            if (addressedAt?.let { it.distanceTo(location) < REFRESH_DISTANCE_METERS } == true) return@collect
            reverseGeocodingRepository.getAddress(location.latitude, location.longitude).onSuccess { address ->
                addressedAt = location
                emit(address)
            }
        }
    }

    private companion object {
        const val REFRESH_DISTANCE_METERS = 50.0
    }
}
