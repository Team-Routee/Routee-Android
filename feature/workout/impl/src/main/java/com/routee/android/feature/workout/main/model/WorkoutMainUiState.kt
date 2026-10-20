package com.routee.android.feature.workout.main.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class WorkoutMainUiState(
    val isLocationPermissionGranted: Boolean = true,
    val address: String = "",
)
