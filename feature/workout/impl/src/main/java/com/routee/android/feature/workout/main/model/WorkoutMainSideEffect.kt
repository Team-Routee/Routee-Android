package com.routee.android.feature.workout.main.model

import com.routee.android.core.map.model.MapPoint

internal sealed interface WorkoutMainSideEffect {
    data object NavigateToRecording : WorkoutMainSideEffect

    /** 복구 모달에서 이어서 기록, 카운트다운 뒤 기록 중으로 */
    data object NavigateToRecoveredRecording : WorkoutMainSideEffect

    data object ShowRecoveryFailed : WorkoutMainSideEffect

    data object ShowRecoverySaved : WorkoutMainSideEffect

    data object ShowRecoverySaveFailed : WorkoutMainSideEffect

    data object RequestLocationPermission : WorkoutMainSideEffect

    /** 이미 허용했거나 Android 13 미만이면 요청 없이 결과로 넘김 */
    data object RequestNotificationPermission : WorkoutMainSideEffect

    /** [from]이 있으면 그 위치로 먼저 옮긴 뒤 내 위치 따라가기 */
    data class FollowMyLocation(val from: MapPoint?) : WorkoutMainSideEffect
}
