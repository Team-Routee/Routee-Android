package com.routee.android.feature.setting

import com.routee.android.core.data.image.repository.ImageCacheRepository
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.auth.ClearDeviceDataUseCase
import java.io.File
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal fun clearDeviceData(records: FakeWorkoutRecordRepository = FakeWorkoutRecordRepository()) =
    ClearDeviceDataUseCase(
        trackingRepository = FakeWorkoutTrackingRepository(),
        recordRepository = records,
        photoRepository = FakeWorkoutPhotoRepository(),
        imageCacheRepository = FakeImageCacheRepository(),
    )

internal class FakeWorkoutTrackingRepository : WorkoutTrackingRepository {
    override val state: StateFlow<WorkoutTrackingState> = MutableStateFlow(WorkoutTrackingState())

    override suspend fun start(
        workout: StartedWorkout,
        type: WorkoutType,
        startedAt: Instant,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun pause(): Result<Unit> = Result.success(Unit)

    override suspend fun resume(): Result<Unit> = Result.success(Unit)

    override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> = Result.success(Unit)

    override suspend fun restore(): Result<Unit> = Result.success(Unit)

    override suspend fun recover(): Result<Unit> = Result.success(Unit)

    override suspend fun finish(endedAt: Instant?): Result<Unit> = Result.success(Unit)

    override suspend fun clear(): Result<Unit> = Result.success(Unit)
}

internal class FakeWorkoutRecordRepository : WorkoutRecordRepository {
    var isCleared = false

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
    ): Result<WorkoutRecord> = Result.success(record)

    override suspend fun deleteRecord(activityId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun clearRecords(): Result<Unit> {
        isCleared = true
        return Result.success(Unit)
    }
}

internal class FakeWorkoutPhotoRepository : WorkoutPhotoRepository {
    override val photos: StateFlow<List<WorkoutPhoto>> = MutableStateFlow(emptyList())

    override suspend fun createCaptureFile(): Result<File> = Result.success(File("capture.jpg"))

    override suspend fun addPhoto(
        activityId: Long,
        original: File,
        point: TrackPoint,
        takenAt: Instant,
    ): Result<WorkoutPhoto> = Result.failure(UnsupportedOperationException())

    override suspend fun discardCaptureFile(file: File) = Unit

    override fun updateTitle(
        photoId: String,
        title: String,
    ) = Unit

    override fun removePhoto(photoId: String) = Unit

    override suspend fun syncPhotos(activityId: Long): Int = 0

    override suspend fun clear(): Result<Unit> = Result.success(Unit)
}

internal class FakeImageCacheRepository : ImageCacheRepository {
    override suspend fun clear(): Result<Unit> = Result.success(Unit)
}
