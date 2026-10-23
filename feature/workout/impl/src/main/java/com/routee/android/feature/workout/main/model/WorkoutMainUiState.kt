package com.routee.android.feature.workout.main.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class WorkoutMainUiState(
    val locationPermission: LocationPermission = LocationPermission.DENIED,
    val isLocationPermissionDialogVisible: Boolean = false,
    val address: String? = null,
)

internal enum class LocationPermission {
    GRANTED,
    APPROXIMATE,
    DENIED,
}
