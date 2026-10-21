package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord

/** 기기에 저장하는 운동 기록, 최대 1개만 두고 새로 시작하면 이전 기록 삭제 */
interface WorkoutRecordRepository {
    suspend fun getRecord(): WorkoutRecord?

    /** 새 기록 시작, 남아 있던 기록과 지도 캡처는 삭제 */
    suspend fun startRecord(record: WorkoutRecord)

    suspend fun saveRecord(record: WorkoutRecord)

    /** 경로 점과 갱신된 기록(거리, 마지막 기록 시각)을 함께 저장 */
    suspend fun saveTrackPoint(
        record: WorkoutRecord,
        point: TrackPoint,
    )

    suspend fun getTrackPoints(activityId: Long): List<TrackPoint>

    /** 지도 캡처 저장, 파일 경로를 넣은 기록 반환 */
    suspend fun saveMapImage(
        record: WorkoutRecord,
        jpeg: ByteArray,
    ): WorkoutRecord

    /** 기록, 경로 점, 지도 캡처 삭제 */
    suspend fun deleteRecord(activityId: Long)

    suspend fun clearRecords()
}
