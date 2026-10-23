package com.routee.android.feature.workout.recording.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.map.model.MapPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal const val MAX_PHOTO_COUNT = 20

internal enum class WorkoutMode {
    COUNTDOWN,
    RECORDING,
    PAUSED,
    FINISHING,
    ;

    val isTracking: Boolean get() = this == RECORDING || this == PAUSED
}

internal fun WorkoutStatus.toMode(): WorkoutMode? = when (this) {
    WorkoutStatus.ACTIVITY_IN_PROGRESS -> WorkoutMode.RECORDING
    WorkoutStatus.ACTIVITY_PAUSED -> WorkoutMode.PAUSED
    WorkoutStatus.PENDING_UPLOAD -> null
}

@Immutable
internal data class WorkoutRecordingUiState(
    val mode: WorkoutMode = WorkoutMode.COUNTDOWN,
    val distanceMeters: Int = 0,
    val durationSec: Int = 0,
    val maxElevationMeters: Int = 0,
    val route: ImmutableList<MapPoint> = persistentListOf(),
    val photoCount: Int = 0,
) {
    val isCameraEnabled: Boolean get() = photoCount < MAX_PHOTO_COUNT
}
