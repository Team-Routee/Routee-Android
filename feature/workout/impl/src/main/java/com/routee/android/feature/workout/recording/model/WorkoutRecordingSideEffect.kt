package com.routee.android.feature.workout.recording.model

internal sealed interface WorkoutRecordingSideEffect {
    data object StartTrackingService : WorkoutRecordingSideEffect

    data object NavigateBack : WorkoutRecordingSideEffect

    data object NavigateToTimeline : WorkoutRecordingSideEffect
}
