package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord

/** 기기에 저장하는 운동 기록, 최대 1개만 두고 새로 시작하면 이전 기록 삭제 */
interface WorkoutRecordRepository {
    suspend fun getRecord(): Result<WorkoutRecord?>

    /** 새 기록 시작, 남아 있던 기록은 삭제 */
    suspend fun startRecord(record: WorkoutRecord): Result<Unit>

    suspend fun saveRecord(record: WorkoutRecord): Result<Unit>

    /** 경로 좌표와 갱신된 기록(거리, 마지막 기록 시각)을 함께 저장 */
    suspend fun saveTrackPoint(
        record: WorkoutRecord,
        point: TrackPoint,
    ): Result<Unit>

    suspend fun getTrackPoints(activityId: Long): Result<List<TrackPoint>>

    /** 기록, 경로 좌표 삭제 */
    suspend fun deleteRecord(activityId: Long): Result<Unit>

    suspend fun clearRecords(): Result<Unit>
}
