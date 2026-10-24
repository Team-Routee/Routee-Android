package com.routee.android.feature.workout

import android.graphics.Bitmap
import com.routee.android.core.data.geocoding.repository.ReverseGeocodingRepository
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.permission.repository.PermissionRepository
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
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
    private val savedTrackPoints: List<TrackPoint> = emptyList(),
) : WorkoutTrackingRepository {
    private val _state = MutableStateFlow(WorkoutTrackingState(record = record))
    override val state: StateFlow<WorkoutTrackingState> = _state
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

    override suspend fun restore(): Result<Unit> = Result.success(Unit)

    override suspend fun recover(): Result<Unit> {
        val current = _state.value
        val record = current.record
        if (record != null) {
            _state.value = current.copy(record = record.recovered(START))
        } else if (savedRecord?.isTracking == true) {
            _state.value = WorkoutTrackingState(record = savedRecord.recovered(START), trackPoints = savedTrackPoints)
        }
        return Result.success(Unit)
    }

    override suspend fun finish(endedAt: Instant?): Result<Unit> {
        _state.value = _state.value.copy(record = _state.value.record?.finished(START, endedAt ?: START))
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

internal class FakeWorkoutRecordRepository(
    private val record: WorkoutRecord? = null,
    private val getError: Throwable? = null,
) : WorkoutRecordRepository {
    var isCleared = false

    override suspend fun getRecord(): Result<WorkoutRecord?> =
        getError?.let { Result.failure(it) } ?: Result.success(record)

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

    override suspend fun clearRecords(): Result<Unit> {
        isCleared = true
        return Result.success(Unit)
    }
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

internal class FakeWorkoutPhotoRepository(
    private val missingCount: Int = 0,
    private val serverPhotos: List<WorkoutPhoto> = emptyList(),
) : WorkoutPhotoRepository {
    private val _photos = MutableStateFlow<List<WorkoutPhoto>>(emptyList())
    override val photos: StateFlow<List<WorkoutPhoto>> = _photos
    val discarded = mutableListOf<File>()
    var captureFileCount = 0
    val restoredActivityIds = mutableListOf<Long>()

    fun setPhotos(photos: List<WorkoutPhoto>) {
        _photos.value = photos
    }

    override suspend fun createCaptureFile(): Result<File> {
        captureFileCount++
        return Result.success(File("capture-$captureFileCount.jpg"))
    }

    override suspend fun addPhoto(
        activityId: Long,
        original: File,
        point: TrackPoint,
        takenAt: Instant,
    ): Result<WorkoutPhoto> {
        val photo = WorkoutPhoto(
            id = "photo-${_photos.value.size + 1}",
            activityId = activityId,
            filePath = original.path,
            point = point,
            takenAt = takenAt,
        )
        _photos.value += photo
        return Result.success(photo)
    }

    override suspend fun restore(activityId: Long): Result<Unit> {
        restoredActivityIds += activityId
        if (_photos.value.none { it.activityId == activityId }) _photos.value = serverPhotos
        return Result.success(Unit)
    }

    override suspend fun discardCaptureFile(file: File) {
        discarded += file
    }

    override fun updateTitle(
        photoId: String,
        title: String,
    ) {
        _photos.value = _photos.value.map { if (it.id == photoId) it.copy(title = title) else it }
    }

    override fun removePhoto(photoId: String) {
        _photos.value = _photos.value.filterNot { it.id == photoId }
    }

    override suspend fun syncPhotos(activityId: Long): Int = missingCount

    override suspend fun clear(): Result<Unit> {
        _photos.value = emptyList()
        return Result.success(Unit)
    }
}

internal class FakeLocationRepository(
    private val locations: Flow<DeviceLocation> = emptyFlow(),
    private val lastLocation: DeviceLocation? = null,
) : LocationRepository {
    override fun observeLocations(): Flow<DeviceLocation> = locations

    override suspend fun getLastLocation(): Result<DeviceLocation?> = Result.success(lastLocation)
}

internal class FakePermissionRepository(var isLocationRequested: Boolean = false) : PermissionRepository {
    override suspend fun getLocationPermissionRequested(): Result<Boolean> = Result.success(isLocationRequested)

    override suspend fun saveLocationPermissionRequested(): Result<Unit> {
        isLocationRequested = true
        return Result.success(Unit)
    }
}

internal class FakeReverseGeocodingRepository(private val address: String = "영등포구 의사당대로 1") :
    ReverseGeocodingRepository {
    override suspend fun getAddress(latitude: Double, longitude: Double): Result<String> = Result.success(address)
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

internal fun photo(id: String) = WorkoutPhoto(
    id = id,
    activityId = 1,
    filePath = "$id.jpg",
    point = trackPoint(1),
    takenAt = START,
    title = "말바위",
)

internal fun trackPoint(index: Int) = TrackPoint(
    pointIndex = index,
    latitude = 37.0 + index,
    longitude = 127.0,
    elevation = 100.0,
    recordedAt = START.plusSeconds(index.toLong()),
)
