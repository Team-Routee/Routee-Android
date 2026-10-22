package com.routee.android.core.data.recap.model

/**
 * 운동 경로와 경로 위 사진 위치
 *
 * @param trackPoints     경로 좌표
 * @param timelineMarkers 사진을 찍은 위치
 */
data class ActivityTrack(
    val trackPoints: List<TrackPoint>,
    val timelineMarkers: List<TimelineMarker>,
)

/**
 * @param pointIndex 좌표 기록 순서
 */
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val pointIndex: Int,
)

/**
 * @param pointIndex 사진을 찍은 위치와 같은 경로 좌표의 순서
 */
data class TimelineMarker(
    val timelineId: Long,
    val thumbnailUrl: String,
    val latitude: Double,
    val longitude: Double,
    val pointIndex: Int,
)
