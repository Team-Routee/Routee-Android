package com.routee.android.core.data.workout.local

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity

internal fun WorkoutEntity.toModel() = WorkoutRecord(
    activityId = id,
    title = title,
    type = WorkoutType.valueOf(type),
    status = WorkoutStatus.valueOf(status),
    startedAt = startedAt,
    durationMillis = durationMillis,
    segmentStartedAt = segmentStartedAt,
    pausedAt = pausedAt,
    lastRecordedAt = lastRecordedAt,
    distanceMeters = distance,
    maxElevationMeters = maxElevation,
    endedAt = endedAt,
    mapImageObjectKey = mapImageObjectKey,
    mapImagePath = mapImagePath,
)

internal fun WorkoutRecord.toEntity() = WorkoutEntity(
    id = activityId,
    title = title,
    type = type.name,
    status = status.name,
    startedAt = startedAt,
    durationMillis = durationMillis,
    segmentStartedAt = segmentStartedAt,
    pausedAt = pausedAt,
    lastRecordedAt = lastRecordedAt,
    distance = distanceMeters,
    maxElevation = maxElevationMeters,
    endedAt = endedAt,
    mapImageObjectKey = mapImageObjectKey,
    mapImagePath = mapImagePath,
)

internal fun TrackPointEntity.toModel() = TrackPoint(
    pointIndex = pointIndex,
    latitude = latitude,
    longitude = longitude,
    elevation = elevation,
    recordedAt = recordedAt,
)

internal fun TrackPoint.toEntity(activityId: Long) = TrackPointEntity(
    workoutId = activityId,
    pointIndex = pointIndex,
    latitude = latitude,
    longitude = longitude,
    elevation = elevation,
    recordedAt = recordedAt,
)
