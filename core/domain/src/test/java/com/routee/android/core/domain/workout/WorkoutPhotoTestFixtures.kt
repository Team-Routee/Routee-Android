package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

internal val PHOTO_START: Instant = Instant.ofEpochMilli(1_000_000)
internal val PHOTO_NOW: Instant = PHOTO_START.plusSeconds(60)

internal val PHOTO_LOCATION = DeviceLocation(
    latitude = 37.0,
    longitude = 127.0,
    altitude = 90.0,
    altitudeAccuracyMeters = 5f,
    accuracyMeters = 5f,
    time = PHOTO_NOW,
)

internal fun photoTrackPoint(index: Int) = TrackPoint(
    pointIndex = index,
    latitude = 37.0,
    longitude = 127.0,
    elevation = 90.0,
    recordedAt = PHOTO_START,
)

internal fun workoutPhoto(
    index: Int,
    activityId: Long = 7,
) = WorkoutPhoto(
    id = "photo-$index",
    activityId = activityId,
    filePath = "photo-$index.jpg",
    point = photoTrackPoint(1),
    takenAt = PHOTO_START,
)

internal class FakePhotoTrackingRepository(
    points: Int,
    status: WorkoutStatus = WorkoutStatus.ACTIVITY_IN_PROGRESS,
) : WorkoutTrackingRepository {
    private val _state = MutableStateFlow(
        WorkoutTrackingState(record = RECORD.copy(status = status), trackPoints = (1..points).map(::photoTrackPoint)),
    )
    override val state: StateFlow<WorkoutTrackingState> = _state
    val added = mutableListOf<DeviceLocation>()

    override suspend fun start(
        workout: StartedWorkout,
        type: WorkoutType,
        startedAt: Instant,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun pause(): Result<Unit> = Result.success(Unit)

    override suspend fun resume(): Result<Unit> = Result.success(Unit)

    override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> {
        added += location
        _state.value = _state.value.copy(trackPoints = _state.value.trackPoints + photoTrackPoint(1))
        return Result.success(Unit)
    }

    override suspend fun restore(): Result<Unit> = Result.success(Unit)

    override suspend fun recover(): Result<Unit> = Result.success(Unit)

    override suspend fun loadPendingUpload(): Result<Unit> = Result.success(Unit)

    override suspend fun finish(endedAt: Instant?): Result<Unit> = Result.success(Unit)

    override suspend fun clear(): Result<Unit> = Result.success(Unit)

    private companion object {
        val RECORD = WorkoutRecord(
            activityId = 7,
            title = "2026.10.24 기록",
            type = WorkoutType.HIKING,
            status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
            startedAt = PHOTO_START,
            durationMillis = 0,
            resumedAt = PHOTO_START,
            pausedAt = null,
            lastRecordedAt = PHOTO_START,
            distanceMeters = 0.0,
            maxElevationMeters = null,
            endedAt = null,
            mapImageObjectKey = null,
            mapImagePath = null,
        )
    }
}

internal class FakeLastLocationRepository(private val lastLocation: DeviceLocation?) : LocationRepository {
    override fun observeLocations(): Flow<DeviceLocation> = emptyFlow()

    override suspend fun getLastLocation(): Result<DeviceLocation?> = Result.success(lastLocation)
}
