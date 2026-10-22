package com.routee.android.core.data.location.local

import android.content.Context
import android.location.Location
import androidx.core.location.LocationCompat
import androidx.core.location.altitude.AltitudeConverterCompat
import com.routee.android.core.data.location.model.DeviceLocation
import java.time.Instant

/** 기기 위치로 변환, 고도는 해수면 기준. 못 바꾸거나 오차를 모르면 null. 디스크를 읽어서 IO에서 부름 */
internal fun Location.toDeviceLocation(context: Context): DeviceLocation {
    if (hasAltitude() && !LocationCompat.hasMslAltitude(this)) {
        runCatching { AltitudeConverterCompat.addMslAltitudeToLocation(context, this) }
    }
    val hasMslAltitude = LocationCompat.hasMslAltitude(this)
    return DeviceLocation(
        latitude = latitude,
        longitude = longitude,
        altitude = if (hasMslAltitude) LocationCompat.getMslAltitudeMeters(this) else null,
        altitudeAccuracyMeters = when {
            hasMslAltitude && LocationCompat.hasMslAltitudeAccuracy(this) ->
                LocationCompat.getMslAltitudeAccuracyMeters(this)

            hasVerticalAccuracy() -> verticalAccuracyMeters

            else -> null
        },
        accuracyMeters = if (hasAccuracy()) accuracy else null,
        time = Instant.ofEpochMilli(time),
    )
}
