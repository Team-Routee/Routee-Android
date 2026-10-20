package com.routee.android.core.data.timeline.model

/** 완료할 운동의 경로 좌표 */
data class TimelineTrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Int,
    val pointIndex: Int,
)

/** 운동 완료와 루트 저장에 필요한 기록 */
data class TimelineCompletion(
    val activityId: Long,
    val title: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val maxElevationMeters: Int,
    val endedAtEpochMillis: Long,
    val track: List<TimelineTrackPoint>,
    val routeNames: List<String>,
)
