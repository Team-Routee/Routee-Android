package com.routee.android.feature.workout.recording.model

import androidx.compose.runtime.Immutable

internal const val MAX_PHOTO_COUNT = 20

internal enum class WorkoutMode {
    COUNTDOWN,
    RECORDING,
    PAUSED,
    FINISHING,
}

@Immutable
internal data class WorkoutRecordingUiState(
    val mode: WorkoutMode = WorkoutMode.COUNTDOWN,
    val distanceMeters: Int = 0,
    val durationSec: Int = 0,
    val maxElevationMeters: Int = 0,
    val photoCount: Int = 0,
) {
    val isCameraEnabled: Boolean get() = photoCount < MAX_PHOTO_COUNT
}
