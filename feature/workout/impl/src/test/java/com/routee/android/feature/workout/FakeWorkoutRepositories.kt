package com.routee.android.feature.workout

import android.graphics.Bitmap
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

internal val START: Instant = Instant.ofEpochMilli(1_000_000)

internal class FakeWorkoutTrackingRepository(
    record: WorkoutRecord? = null,
    private val savedRecord: WorkoutRecord? = null,
    private val restoreError: Throwable? = null,
) : WorkoutTrackingRepository {
    private val _state = MutableStateFlow(WorkoutTrackingState(record = record))
    override val state: StateFlow<WorkoutTrackingState> = _state
    var isRestored = false
    var isCleared = false
    var startedRecord: WorkoutRecord? = null

    fun setState(state: WorkoutTrackingState) {
        _state.value = state
    }

    override suspend fun start(
        workout: StartedWorkout,
        type: WorkoutType,
        startedAt: Instant,
    ): Result<Unit> {
        val record = WorkoutRecord.started(workout, type, startedAt, START)
        startedRecord = record
        _state.value = WorkoutTrackingState(record = record)
        return Result.success(Unit)
    }

    override suspend fun pause(): Result<Unit> {
        _state.value = _state.value.copy(record = _state.value.record?.paused(START))
        return Result.success(Unit)
    }

    override suspend fun resume(): Result<Unit> {
        _state.value = _state.value.copy(record = _state.value.record?.resumed(START))
        return Result.success(Unit)
    }

    override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> = Result.success(Unit)

    override suspend fun restore(): Result<Unit> {
        isRestored = true
        restoreError?.let { return Result.failure(it) }
        if (_state.value.record == null && savedRecord?.isTracking == true) {
            _state.value = WorkoutTrackingState(record = savedRecord)
        }
        return Result.success(Unit)
    }

    override suspend fun finish(): Result<Unit> {
        _state.value = _state.value.copy(record = _state.value.record?.finished(START))
        return Result.success(Unit)
    }

    override suspend fun clear(): Result<Unit> {
        isCleared = true
        _state.value = WorkoutTrackingState()
        return Result.success(Unit)
    }
}

internal class FakeWorkoutRemoteRepository(
    private val createError: Throwable? = null,
) : WorkoutRemoteRepository {
    override suspend fun createWorkout(
        type: WorkoutType,
        startedAt: Instant,
    ): Result<StartedWorkout> =
        createError?.let { Result.failure(it) }
            ?: Result.success(StartedWorkout(activityId = 1, title = "2026.10.23 기록"))

    override suspend fun updateStatus(
        activityId: Long,
        status: WorkoutStatus,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun completeWorkout(
        record: WorkoutRecord,
        trackPoints: List<TrackPoint>,
        mapImageObjectKey: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun updateRoutes(
        activityId: Long,
        names: List<String>,
    ): Result<Unit> = Result.success(Unit)
}

internal class FakeWorkoutRecordRepository : WorkoutRecordRepository {
    override suspend fun getRecord(): Result<WorkoutRecord?> = Result.success(null)

    override suspend fun startRecord(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

    override suspend fun saveRecord(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

    override suspend fun saveTrackPoint(
        record: WorkoutRecord,
        point: TrackPoint,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getTrackPoints(activityId: Long): Result<List<TrackPoint>> = Result.success(emptyList())

    override suspend fun saveMapImage(
        record: WorkoutRecord,
        jpeg: ByteArray,
    ): Result<WorkoutRecord> = Result.success(record.copy(mapImagePath = "map/${record.activityId}.jpg"))

    override suspend fun deleteRecord(activityId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun clearRecords(): Result<Unit> = Result.success(Unit)
}

internal class FakeMapImageRepository : TimelineMapImageRepository {
    override suspend fun uploadMapImage(
        activityId: Long,
        bitmap: Bitmap,
    ): Result<String> = Result.success("map-key")

    override suspend fun uploadMapImage(
        activityId: Long,
        imageFile: File,
    ): Result<String> = Result.success("map-key")
}

internal class FakeLocationRepository : LocationRepository {
    override fun observeLocations(): Flow<DeviceLocation> = emptyFlow()
}

internal class TestClock(var now: Instant = START) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId?): Clock = this

    override fun instant(): Instant = now
}

internal fun record(status: WorkoutStatus = WorkoutStatus.ACTIVITY_IN_PROGRESS) = WorkoutRecord(
    activityId = 1,
    title = "2026.10.23 기록",
    type = WorkoutType.HIKING,
    status = status,
    startedAt = START,
    durationMillis = 0,
    resumedAt = START.takeIf { status == WorkoutStatus.ACTIVITY_IN_PROGRESS },
    pausedAt = null,
    lastRecordedAt = START,
    distanceMeters = 1_234.4,
    maxElevationMeters = 302.6,
    endedAt = null,
    mapImageObjectKey = null,
    mapImagePath = null,
)

internal fun trackPoint(index: Int) = TrackPoint(
    pointIndex = index,
    latitude = 37.0 + index,
    longitude = 127.0,
    elevation = 100.0,
    recordedAt = START.plusSeconds(index.toLong()),
)
