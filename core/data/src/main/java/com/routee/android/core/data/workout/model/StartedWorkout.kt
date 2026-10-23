package com.routee.android.core.data.workout.model

/**
 * 시작한 운동 기록
 *
 * @param title 기본 제목 (예: "2026.10.23 기록")
 */
data class StartedWorkout(
    val activityId: Long,
    val title: String,
)
