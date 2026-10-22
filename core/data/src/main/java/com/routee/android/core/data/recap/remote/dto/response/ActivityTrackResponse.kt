package com.routee.android.core.data.recap.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ActivityTrackResponse(
    @SerialName("activityId")
    val activityId: Long,
    @SerialName("trackPoints")
    val trackPoints: List<TrackPointResponse> = emptyList(),
    @SerialName("timelineMarkers")
    val timelineMarkers: List<TimelineMarkerResponse> = emptyList(),
)

@Serializable
internal data class TrackPointResponse(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("elevation")
    val elevation: Double,
    @SerialName("pointIndex")
    val pointIndex: Int,
)

@Serializable
internal data class TimelineMarkerResponse(
    @SerialName("timelineId")
    val timelineId: Long,
    @SerialName("thumbnailUrl")
    val thumbnailUrl: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("pointIndex")
    val pointIndex: Int,
)
