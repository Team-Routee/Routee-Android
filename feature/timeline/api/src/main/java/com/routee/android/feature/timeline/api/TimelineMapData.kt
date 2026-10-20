package com.routee.android.feature.timeline.api

import kotlinx.serialization.Serializable

/** [TimelineNavKey]로 전달할 운동 경로 좌표 */
@Serializable
data class TimelineMapPoint(
    val latitude: Double,
    val longitude: Double,
)

/** [TimelineNavKey]로 전달할 사진 촬영 좌표와 이미지 URL */
@Serializable
data class TimelineMapPhoto(
    val imageUrl: String,
    val point: TimelineMapPoint,
)
