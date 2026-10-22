package com.routee.android.core.data.workout.model

/**
 * 기기에 저장된 운동 기록 상태
 *
 * - `ACTIVITY_IN_PROGRESS`: 기록 중
 * - `ACTIVITY_PAUSED`: 일시정지
 * - `PENDING_UPLOAD`: 종료를 눌렀고 서버 저장 확인 전
 */
enum class WorkoutStatus {
    ACTIVITY_IN_PROGRESS,
    ACTIVITY_PAUSED,
    PENDING_UPLOAD,
}
