package com.routee.android.core.data.workout.model

import java.time.Instant

/** 경로 점 */
data class TrackPoint(
    val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val recordedAt: Instant,
)
