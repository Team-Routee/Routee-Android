package com.routee.android.core.data.workout.local

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity
import com.routee.android.core.database.entity.WorkoutPhotoEntity

internal fun WorkoutEntity.toModel() = WorkoutRecord(
    activityId = id,
    title = title,
    type = WorkoutType.valueOf(type),
    status = WorkoutStatus.valueOf(status),
    startedAt = startedAt,
    durationMillis = durationMillis,
    resumedAt = resumedAt,
    pausedAt = pausedAt,
    lastRecordedAt = lastRecordedAt,
    distanceMeters = distance,
    maxElevationMeters = maxElevation,
    endedAt = endedAt,
)

internal fun WorkoutRecord.toEntity() = WorkoutEntity(
    id = activityId,
    title = title,
    type = type.name,
    status = status.name,
    startedAt = startedAt,
    durationMillis = durationMillis,
    resumedAt = resumedAt,
    pausedAt = pausedAt,
    lastRecordedAt = lastRecordedAt,
    distance = distanceMeters,
    maxElevation = maxElevationMeters,
    endedAt = endedAt,
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

internal fun WorkoutPhotoEntity.toModel() = WorkoutPhoto(
    id = id,
    activityId = workoutId,
    filePath = filePath,
    point = TrackPoint(
        pointIndex = pointIndex,
        latitude = latitude,
        longitude = longitude,
        elevation = elevation,
        recordedAt = recordedAt,
    ),
    takenAt = takenAt,
    title = title,
    timelineId = timelineId,
    serverTitle = serverTitle,
    serverImageUrl = serverImageUrl,
)

internal fun WorkoutPhoto.toEntity() = WorkoutPhotoEntity(
    id = id,
    workoutId = activityId,
    filePath = filePath,
    pointIndex = point.pointIndex,
    latitude = point.latitude,
    longitude = point.longitude,
    elevation = point.elevation,
    recordedAt = point.recordedAt,
    takenAt = takenAt,
    title = title,
    timelineId = timelineId,
    serverTitle = serverTitle,
    serverImageUrl = serverImageUrl,
)
