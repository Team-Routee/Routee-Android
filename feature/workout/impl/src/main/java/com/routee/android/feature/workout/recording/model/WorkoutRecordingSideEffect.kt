package com.routee.android.feature.workout.recording.model

import com.routee.android.core.map.model.MapPoint

internal sealed interface WorkoutRecordingSideEffect {
    data object StartTrackingService : WorkoutRecordingSideEffect

    data object StartFailed : WorkoutRecordingSideEffect

    data object ShowRestoreFailed : WorkoutRecordingSideEffect

    data class NavigateToTimeline(val summary: WorkoutSummary) : WorkoutRecordingSideEffect

    data object NavigateToHome : WorkoutRecordingSideEffect

    data class MoveToLastLocation(val point: MapPoint) : WorkoutRecordingSideEffect
}
