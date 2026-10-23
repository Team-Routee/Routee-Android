package com.routee.android.feature.workout.main.model

internal sealed interface WorkoutMainSideEffect {
    data object NavigateToRecording : WorkoutMainSideEffect
}
