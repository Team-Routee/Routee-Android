package com.routee.android.core.data.workout.model

import java.time.Instant

/**
 * 운동 중 찍은 사진
 *
 * @param id          기기에서 만든 식별자, 지도 마커와 재전송에 사용
 * @param filePath    압축한 JPEG 파일 경로
 * @param point       찍은 위치에 연결한 경로 좌표
 * @param timelineId  타임라인 등록 결과, 아직 못 올렸으면 null
 */
data class WorkoutPhoto(
    val id: String,
    val activityId: Long,
    val filePath: String,
    val point: TrackPoint,
    val takenAt: Instant,
    val title: String = "",
    val timelineId: Long? = null,
)
