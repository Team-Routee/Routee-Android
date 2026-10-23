package com.routee.android.feature.workout.recording.model

import com.routee.android.core.data.workout.model.WorkoutRecord
import kotlin.math.roundToInt

internal data class WorkoutSummary(
    val activityId: Long,
    val endedAtEpochMillis: Long,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val maxElevationMeters: Int,
)

internal fun WorkoutRecord.toSummary(): WorkoutSummary? {
    val endedAt = endedAt ?: return null
    return WorkoutSummary(
        activityId = activityId,
        endedAtEpochMillis = endedAt.toEpochMilli(),
        distanceMeters = distanceMeters.roundToInt(),
        durationSeconds = durationSecondsAt(endedAt),
        maxElevationMeters = maxElevationMeters?.roundToInt() ?: 0,
    )
}
