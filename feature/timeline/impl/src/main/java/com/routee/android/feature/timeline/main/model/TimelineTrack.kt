package com.routee.android.feature.timeline.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint

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
