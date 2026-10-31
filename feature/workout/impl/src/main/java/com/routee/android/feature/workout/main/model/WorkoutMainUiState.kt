package com.routee.android.feature.workout.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.map.model.MapPoint
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class WorkoutMainUiState(
    val locationPermission: LocationPermission = LocationPermission.DENIED,
    val isLocationPermissionDialogVisible: Boolean = false,
    val isLocationServiceEnabled: Boolean = true,
    val isLocationServiceDialogVisible: Boolean = false,
    val address: String? = null,
    // 복구 모달, 없으면 null
    val recovery: WorkoutRecoveryUiModel? = null,
    val isRecoverySaving: Boolean = false,
    // 종료 후 저장, 재전송 지도 캡처 요청, 캡처를 받으면 null
    val recoveryCaptureRoute: ImmutableList<MapPoint>? = null,
    // 복구 모달에서 이어서 기록, X를 눌러 처리 중, 모달을 내림
    val isRecoveryInProgress: Boolean = false,
    // 진입 때 남은 기록 확인, 재전송 중이면 true, 새 기록이 남은 기록과 겹치지 않게 운동 기록 비활성화
    val isCheckingUnfinishedWorkout: Boolean = true,
) {
    // 위치 권한, 위치 서비스 안내 모달이 닫히거나 처리가 실패하면 다시 보여줌
    val isRecoveryDialogVisible: Boolean
        get() = recovery != null && !isLocationGuideDialogVisible && !isRecoveryInProgress

    // 위치 권한, 위치 서비스 안내 모달이 떠 있을 때는 로딩을 내림
    val isLoadingVisible: Boolean
        get() = isRecoverySaving || (isRecoveryInProgress && !isLocationGuideDialogVisible)

    private val isLocationGuideDialogVisible: Boolean
        get() = isLocationPermissionDialogVisible || isLocationServiceDialogVisible
}

internal enum class LocationPermission {
    GRANTED,
    APPROXIMATE,
    DENIED,
}
