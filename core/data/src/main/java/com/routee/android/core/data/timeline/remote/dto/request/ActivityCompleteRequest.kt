package com.routee.android.core.data.timeline.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
internal data class ActivityCompleteRequest(
    val title: String,
    val distance: Int,
    val durationSec: Int,
    val maxElevation: Int,
    val mapImageObjectKey: String,
    val track: List<ActivityTrackPointRequest>,
    val endedAt: String,
)

@Serializable
internal data class ActivityTrackPointRequest(
    val latitude: Double,
    val longitude: Double,
    val elevation: Int,
    val pointIndex: Int,
)
