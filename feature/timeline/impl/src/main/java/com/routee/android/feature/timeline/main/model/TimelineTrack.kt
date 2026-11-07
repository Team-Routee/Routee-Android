package com.routee.android.feature.timeline.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint
import java.io.File

/** 타임라인 지도에 그릴 경로와 사진 마커 */
@Immutable
internal data class TimelineTrack(
    val route: List<MapPoint>,
    val photos: List<MapPhoto>,
)

internal fun ActivityTrack.toTimelineTrack() = TimelineTrack(
    route = trackPoints.sortedBy { it.pointIndex }.map { MapPoint(it.latitude, it.longitude) },
    photos = timelineMarkers.map {
        MapPhoto(it.timelineId.toString(), it.thumbnailUrl, MapPoint(it.latitude, it.longitude))
    },
)

/** 기기에 남긴 경로와 사진으로 그린 타임라인 지도 */
internal fun List<TrackPoint>.toTimelineTrack(
    photos: List<WorkoutPhoto>,
    activityId: Long,
) = TimelineTrack(
    route = map { MapPoint(it.latitude, it.longitude) },
    photos = photos.filter { it.activityId == activityId }.mapNotNull { photo ->
        val timelineId = photo.timelineId ?: return@mapNotNull null
        val imageUrl = photo.imageUrl() ?: return@mapNotNull null
        MapPhoto(timelineId.toString(), imageUrl, MapPoint(photo.point.latitude, photo.point.longitude))
    },
)

// 기기 파일이 있으면 file: 주소, 없으면 서버 주소
private fun WorkoutPhoto.imageUrl(): String? = filePath?.let { File(it).toURI().toString() } ?: serverImageUrl
