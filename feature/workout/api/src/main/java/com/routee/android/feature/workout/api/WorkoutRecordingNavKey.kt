package com.routee.android.feature.workout.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 운동 기록 화면을 여는 키
 *
 * @param start 새로 시작할 때 시작 API 결과, 진행 중인 기록을 이어갈 때는 null
 */
@Serializable
data class WorkoutRecordingNavKey(val start: WorkoutStart? = null) : NavKey

/**
 * 시작 API 결과
 *
 * @param startedAtEpochMillis 운동 기록 버튼을 누른 시각
 */
@Serializable
data class WorkoutStart(
    val activityId: Long,
    val title: String,
    val startedAtEpochMillis: Long,
)
