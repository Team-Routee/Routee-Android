package com.routee.android.core.data.workout.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CompleteActivityRequest(
    @SerialName("title")
    val title: String,
    @SerialName("distance")
    val distance: Int,
    @SerialName("durationSec")
    val durationSec: Int,
    @SerialName("maxElevation")
    val maxElevation: Int,
    @SerialName("mapImageObjectKey")
    val mapImageObjectKey: String,
    @SerialName("track")
    val track: List<ActivityTrackPointRequest>,
    @SerialName("endedAt")
    val endedAt: String,
)

@Serializable
internal data class ActivityTrackPointRequest(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("elevation")
    val elevation: Int,
    @SerialName("pointIndex")
    val pointIndex: Int,
)
