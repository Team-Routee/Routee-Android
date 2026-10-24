package com.routee.android.feature.workout.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.map.model.MapPoint
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class WorkoutMainUiState(
    val locationPermission: LocationPermission = LocationPermission.DENIED,
    val isLocationPermissionDialogVisible: Boolean = false,
    val address: String? = null,
    // 복구 모달, 없으면 null
    val recovery: WorkoutRecoveryUiModel? = null,
    val isRecoverySaving: Boolean = false,
    // 종료 후 저장 지도 캡처 요청, 캡처를 받으면 null
    val recoveryCaptureRoute: ImmutableList<MapPoint>? = null,
) {
    // 위치 권한 안내 모달이 떠 있으면 그 뒤에 다시 보여줌
    val isRecoveryDialogVisible: Boolean get() = recovery != null && !isLocationPermissionDialogVisible
}

internal enum class LocationPermission {
    GRANTED,
    APPROXIMATE,
    DENIED,
}
