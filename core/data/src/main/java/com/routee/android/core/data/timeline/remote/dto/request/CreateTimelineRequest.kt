package com.routee.android.core.data.timeline.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateTimelineRequest(
    @SerialName("title") val title: String,
    @SerialName("timelineImageObjectKey") val timelineImageObjectKey: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("trackPointIndex") val trackPointIndex: Int,
    @SerialName("location") val location: TimelineLocationRequest,
    @SerialName("status") val status: String,
)

@Serializable
internal data class TimelineLocationRequest(
    @SerialName("longitude") val longitude: Double,
    @SerialName("latitude") val latitude: Double,
    @SerialName("elevation") val elevation: Int,
    @SerialName("pointIndex") val pointIndex: Int,
)
