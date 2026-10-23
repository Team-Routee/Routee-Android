package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import java.time.Instant

/** 서버 운동 기록 */
interface WorkoutRemoteRepository {
    /** 새 기록 생성 */
    suspend fun createWorkout(
        type: WorkoutType,
        startedAt: Instant,
    ): Result<StartedWorkout>

    /** 기록 중, 일시정지 상태 변경 */
    suspend fun updateStatus(
        activityId: Long,
        status: WorkoutStatus,
    ): Result<Unit>
}
