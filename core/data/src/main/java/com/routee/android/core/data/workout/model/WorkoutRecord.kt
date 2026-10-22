package com.routee.android.core.data.workout.model

import java.time.Duration
import java.time.Instant

/**
 * 기기에 저장된 운동 기록
 *
 * @param durationMillis     마지막 일시정지까지 누적된 운동 시간
 * @param resumedAt          마지막으로 시작하거나 다시 시작한 시각, 일시정지면 null
 * @param pausedAt           일시정지 시각, 2시간 규칙 판단
 * @param lastRecordedAt     마지막 위치 기록 시각, 복구 안내와 종료 후 저장 기준
 * @param endedAt            종료 시각, 완료 API를 다시 보낼 때 사용
 * @param mapImageObjectKey  지도 이미지 업로드 결과, 있으면 업로드 생략
 * @param mapImagePath       지도 캡처 파일 경로
 */
data class WorkoutRecord(
    val activityId: Long,
    val title: String,
    val type: WorkoutType,
    val status: WorkoutStatus,
    val startedAt: Instant,
    val durationMillis: Long,
    val resumedAt: Instant?,
    val pausedAt: Instant?,
    val lastRecordedAt: Instant,
    val distanceMeters: Double,
    val maxElevationMeters: Double?,
    val endedAt: Instant?,
    val mapImageObjectKey: String?,
    val mapImagePath: String?,
) {
    /** 기록 중이거나 일시정지 중, 종료 후 저장 확인 전이면 false */
    val isTracking: Boolean
        get() = status == WorkoutStatus.ACTIVITY_IN_PROGRESS || status == WorkoutStatus.ACTIVITY_PAUSED

    /** [now]까지 운동 시간 (ms) = durationMillis + (now - resumedAt) */
    fun durationMillisAt(now: Instant): Long =
        durationMillis + (resumedAt?.let { Duration.between(it, now).toMillis().coerceAtLeast(0) } ?: 0)

    /** 일시정지, 기록 중일 때만 (durationMillis += now - resumedAt, resumedAt = null) */
    fun paused(now: Instant): WorkoutRecord {
        if (status != WorkoutStatus.ACTIVITY_IN_PROGRESS) return this
        return copy(
            status = WorkoutStatus.ACTIVITY_PAUSED,
            durationMillis = durationMillisAt(now),
            resumedAt = null,
            pausedAt = now,
        )
    }

    /** 다시 시작, 일시정지 중일 때만 (resumedAt = now) */
    fun resumed(now: Instant): WorkoutRecord {
        if (status != WorkoutStatus.ACTIVITY_PAUSED) return this
        return copy(status = WorkoutStatus.ACTIVITY_IN_PROGRESS, resumedAt = now, pausedAt = null)
    }

    /** 앱이 다시 켜져서 이어갈 때, 꺼져 있던 시간은 뺌 (durationMillis += lastRecordedAt - resumedAt, resumedAt = now) */
    fun restarted(now: Instant): WorkoutRecord {
        if (status != WorkoutStatus.ACTIVITY_IN_PROGRESS) return this
        return copy(durationMillis = durationMillisAt(lastRecordedAt), resumedAt = now)
    }

    /** 경로 좌표를 더한 기록, 거리와 최고 고도, 마지막 기록 시각 갱신 */
    fun withTrackPoint(
        point: TrackPoint,
        addedDistanceMeters: Double,
    ): WorkoutRecord = copy(
        distanceMeters = distanceMeters + addedDistanceMeters,
        maxElevationMeters = maxOf(maxElevationMeters ?: point.elevation, point.elevation),
        lastRecordedAt = point.recordedAt,
    )
}
