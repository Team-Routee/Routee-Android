package com.routee.android.feature.workout.recording.model

import android.location.Location
import com.routee.android.core.data.location.model.DeviceLocation

internal fun DeviceLocation.toLocation(): Location = Location(PROVIDER).also {
    it.latitude = latitude
    it.longitude = longitude
    it.time = time.toEpochMilli()
    accuracyMeters?.let { accuracy -> it.accuracy = accuracy }
}

private const val PROVIDER = "routee"
