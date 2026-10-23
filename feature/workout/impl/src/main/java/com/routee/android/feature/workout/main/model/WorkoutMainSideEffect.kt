package com.routee.android.feature.workout.main.model

import com.routee.android.feature.workout.api.WorkoutStart

internal sealed interface WorkoutMainSideEffect {
    data class NavigateToRecording(val start: WorkoutStart? = null) : WorkoutMainSideEffect

    data object StartFailed : WorkoutMainSideEffect
}
