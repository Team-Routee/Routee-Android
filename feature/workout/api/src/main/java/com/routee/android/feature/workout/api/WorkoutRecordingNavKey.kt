package com.routee.android.feature.workout.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 운동 기록 화면을 여는 키
 *
 * @param isRecovering 복구 모달에서 이어서 기록, 카운트다운 뒤 불러온 기록을 다시 시작
 */
@Serializable
data class WorkoutRecordingNavKey(val isRecovering: Boolean = false) : NavKey
