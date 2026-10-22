package com.routee.android.core.data.workout.model

import java.time.Instant

/**
 * 기기에 저장된 운동 기록
 *
 * @param durationMillis     지금 구간 이전까지 누적된 운동 시간
 * @param segmentStartedAt   지금 기록 구간 시작 시각, 일시정지면 null
 * @param pausedAt           일시정지 시각, 2시간 규칙 판단
 * @param lastRecordedAt     마지막 위치 기록 시각, 복구 안내와 종료 후 저장 기준
 * @param endedAt            종료 시각, 완료 API를 다시 보낼 때 그대로 사용
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
    val segmentStartedAt: Instant?,
    val pausedAt: Instant?,
    val lastRecordedAt: Instant,
    val distanceMeters: Double,
    val maxElevationMeters: Double?,
    val endedAt: Instant?,
    val mapImageObjectKey: String?,
    val mapImagePath: String?,
)
