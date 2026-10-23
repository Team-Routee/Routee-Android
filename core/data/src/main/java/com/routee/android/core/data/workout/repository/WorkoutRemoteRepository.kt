package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
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

    /**
     * 기록 완료, 이미 완료된 기록이면 성공
     *
     * @param record            종료 시각이 있는 기록
     * @param mapImageObjectKey 지도 캡처 업로드 결과
     */
    suspend fun completeWorkout(
        record: WorkoutRecord,
        trackPoints: List<TrackPoint>,
        mapImageObjectKey: String,
    ): Result<Unit>
}
