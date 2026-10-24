package com.routee.android.core.data.workout.model

import java.time.Instant

/**
 * 운동 중 찍은 사진
 *
 * @param id              기기에서 만든 식별자, 지도 마커와 재전송에 사용
 * @param filePath        압축한 JPEG 파일 경로, 서버에서 불러온 사진은 null
 * @param point           찍은 위치에 연결한 경로 좌표
 * @param title           장소명, 최대 16자
 * @param timelineId      타임라인 등록 결과, 아직 못 올렸으면 null
 * @param serverTitle     서버에 반영된 장소명, 다르면 다음 전송 때 수정
 * @param serverImageUrl  서버 사진 주소, 기기 파일이 없을 때 표시
 */
data class WorkoutPhoto(
    val id: String,
    val activityId: Long,
    val filePath: String?,
    val point: TrackPoint,
    val takenAt: Instant,
    val title: String = "",
    val timelineId: Long? = null,
    val serverTitle: String? = null,
    val serverImageUrl: String? = null,
)
