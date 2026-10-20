package com.routee.android.feature.timeline.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 운동 종료 결과로 타임라인 화면을 여는 키 */
@Serializable
data class TimelineNavKey(
    val activityId: Long,
    val endedAtEpochMillis: Long,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val maxElevationMeters: Int,
    val photos: List<TimelinePhoto> = emptyList(),
    val photoUploads: List<TimelinePhotoUploadData> = emptyList(),
    val routePoints: List<TimelineMapPoint> = emptyList(),
    val photoMarkers: List<TimelineMapPhoto> = emptyList(),
) : NavKey
