package com.routee.android.core.data.workout.model

import java.time.Instant

/** 앱 진입 때 끝나지 않은 운동 기록 */
sealed interface UnfinishedWorkout {
    data object None : UnfinishedWorkout

    /** 앱이 살아 있는 동안 기록 중이거나 일시정지 2시간 이내, 안내 없이 기록 화면으로 */
    data object Tracking : UnfinishedWorkout

    /** 앱이 꺼졌다 켜졌거나 일시정지가 2시간을 넘음, 이어서 기록과 종료 후 저장을 고르게 함 */
    data class Recoverable(val record: WorkoutRecord) : UnfinishedWorkout

    /** 종료했지만 서버 저장을 확인하지 못함 */
    data class PendingUpload(val record: WorkoutRecord) : UnfinishedWorkout

    companion object {
        /**
         * @param current 앱 메모리의 기록, 앱이 꺼졌다 켜졌으면 null
         * @param saved   기기에 저장된 기록
         */
        fun of(
            current: WorkoutRecord?,
            saved: WorkoutRecord?,
            now: Instant,
        ): UnfinishedWorkout {
            val record = current ?: saved ?: return None
            return when {
                record.status == WorkoutStatus.PENDING_UPLOAD -> PendingUpload(record)
                current == null || record.isPauseExpiredAt(now) -> Recoverable(record)
                else -> Tracking
            }
        }
    }
}
