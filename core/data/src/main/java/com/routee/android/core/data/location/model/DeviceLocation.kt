package com.routee.android.core.data.location.model

import java.time.Instant

/**
 * 기기에서 받은 위치 한 번
 *
 * @param altitude                해수면 기준 고도 (m), 없으면 null
 * @param altitudeAccuracyMeters  고도 오차 (m), 없으면 null
 * @param accuracyMeters          수평 오차 (m), 없으면 null
 */
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val altitudeAccuracyMeters: Float?,
    val accuracyMeters: Float?,
    val time: Instant,
)

/** [other]까지 거리 (m) */
fun DeviceLocation.distanceTo(other: DeviceLocation): Double =
    distanceMeters(latitude, longitude, other.latitude, other.longitude)
