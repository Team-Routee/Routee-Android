package com.routee.android.feature.workout.recording.model

internal sealed interface WorkoutRecordingSideEffect {
    data object NavigateToTimeline : WorkoutRecordingSideEffect
}
