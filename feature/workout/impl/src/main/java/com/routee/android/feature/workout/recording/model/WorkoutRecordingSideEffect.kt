package com.routee.android.feature.workout.recording.model

internal sealed interface WorkoutRecordingSideEffect {
    data object StartTrackingService : WorkoutRecordingSideEffect

    data object StartFailed : WorkoutRecordingSideEffect

    data object NavigateToTimeline : WorkoutRecordingSideEffect
}
