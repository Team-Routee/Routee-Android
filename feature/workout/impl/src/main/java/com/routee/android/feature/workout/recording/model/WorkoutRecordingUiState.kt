package com.routee.android.feature.workout.recording.model

import androidx.compose.runtime.Immutable

internal const val MAX_PHOTO_COUNT = 20

@Immutable
internal data class WorkoutRecordingUiState(
    val distanceMeters: Double = 0.0,
    val durationSec: Int = 0,
    val maxElevationMeters: Double = 0.0,
    val photoCount: Int = 0,
) {
    val isCameraEnabled: Boolean get() = photoCount < MAX_PHOTO_COUNT
}
