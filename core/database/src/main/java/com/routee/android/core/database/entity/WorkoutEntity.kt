package com.routee.android.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * 운동 기록 테이블 (`workout`)
 *
 * @param id                 서버 `activityId`
 * @param title              시작 API가 준 기본 제목
 * @param type               `HIKING`, `RUNNING`
 * @param status             `ACTIVITY_IN_PROGRESS`, `ACTIVITY_PAUSED`, `PENDING_UPLOAD`
 * @param startedAt          시작 시각
 * @param durationMillis     마지막 일시정지까지 누적된 운동 시간
 * @param resumedAt          마지막으로 시작하거나 다시 시작한 시각, 일시정지면 null
 * @param pausedAt           일시정지 시각, 2시간 규칙 판단
 * @param lastRecordedAt     마지막 위치 기록 시각, 복구 안내와 종료 후 저장 기준
 * @param distance           누적 거리 (m)
 * @param maxElevation       최고 고도 (m)
 * @param endedAt            종료 시각, 완료 API를 다시 보낼 때 사용
 * @param mapImageObjectKey  지도 이미지 업로드 결과, 있으면 업로드 생략
 * @param mapImagePath       지도 캡처 파일 경로
 */
@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val type: String,
    val status: String,
    @ColumnInfo(name = "started_at") val startedAt: Instant,
    @ColumnInfo(name = "duration_millis") val durationMillis: Long,
    @ColumnInfo(name = "resumed_at") val resumedAt: Instant?,
    @ColumnInfo(name = "paused_at") val pausedAt: Instant?,
    @ColumnInfo(name = "last_recorded_at") val lastRecordedAt: Instant,
    val distance: Double,
    @ColumnInfo(name = "max_elevation") val maxElevation: Double?,
    @ColumnInfo(name = "ended_at") val endedAt: Instant?,
    @ColumnInfo(name = "map_image_object_key") val mapImageObjectKey: String?,
    @ColumnInfo(name = "map_image_path") val mapImagePath: String?,
)
