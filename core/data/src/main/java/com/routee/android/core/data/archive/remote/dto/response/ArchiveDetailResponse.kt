package com.routee.android.core.data.archive.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
internal data class ActivityStatisticsResponse(
    val activityId: Long,
    val title: String,
    val activityDate: String,
    val distanceMeter: Int,
    val durationSec: Int,
    val maxElevationMeter: Int,
)

@Serializable
internal data class ArchiveTimelineResponse(
    val activityId: Long,
    val timelines: List<ArchivePhotoResponse>,
)

@Serializable
internal data class ArchivePhotoResponse(
    val timelineId: Long,
    val title: String?,
    val imageUrl: String,
    val createdAt: String,
)

@Serializable
internal data class ArchiveRoutesResponse(
    val activityId: Long,
    val routes: List<ArchiveRouteResponse>,
)

@Serializable
internal data class ArchiveRouteResponse(
    val routeId: Long,
    val name: String,
    val sequence: Int,
)
