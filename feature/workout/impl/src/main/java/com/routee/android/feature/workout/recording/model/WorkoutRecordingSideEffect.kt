package com.routee.android.feature.workout.recording.model

internal sealed interface WorkoutRecordingSideEffect {
    data object StartTrackingService : WorkoutRecordingSideEffect

    data object StartFailed : WorkoutRecordingSideEffect

    data object ShowRestoreFailed : WorkoutRecordingSideEffect

    data class NavigateToTimeline(val summary: WorkoutSummary) : WorkoutRecordingSideEffect

    data object SaveFailed : WorkoutRecordingSideEffect

    data object NavigateToHome : WorkoutRecordingSideEffect
}
