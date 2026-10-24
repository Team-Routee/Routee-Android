package com.routee.android.core.data.workout.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.timeline.model.TimelinePhotoStatus
import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.repository.TimelinePhotoRepository
import com.routee.android.core.data.workout.local.WorkoutPhotoLocalDataSource
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.toServerElevation
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class WorkoutPhotoRepositoryImpl @Inject constructor(
    private val localDataSource: WorkoutPhotoLocalDataSource,
    private val timelinePhotoRepository: TimelinePhotoRepository,
) : WorkoutPhotoRepository {
    private val _photos = MutableStateFlow<List<WorkoutPhoto>>(emptyList())
    override val photos: StateFlow<List<WorkoutPhoto>> = _photos.asStateFlow()

    private val mutex = Mutex()

    override suspend fun createCaptureFile(): Result<File> = suspendRunCatching { localDataSource.createCaptureFile() }

    override suspend fun addPhoto(
        activityId: Long,
        original: File,
        point: TrackPoint,
        takenAt: Instant,
    ): Result<WorkoutPhoto> = suspendRunCatching {
        val id = UUID.randomUUID().toString()
        val path = localDataSource.compress(activityId, id, original)
        val photo = WorkoutPhoto(id = id, activityId = activityId, filePath = path, point = point, takenAt = takenAt)
        mutex.withLock {
            val previous = _photos.value
            if (previous.any { it.activityId != activityId }) {
                localDataSource.deleteOthers(activityId)
                previous.map { it.activityId }.distinct().forEach { timelinePhotoRepository.clearUploadState(it) }
                _photos.value = listOf(photo)
            } else {
                _photos.value = previous + photo
            }
        }
        photo
    }

    override suspend fun discardCaptureFile(file: File) = localDataSource.delete(file.path)

    override suspend fun uploadPhotos(activityId: Long): Int {
        val pending = _photos.value.filter { it.activityId == activityId && it.timelineId == null }
        if (pending.isEmpty()) return 0
        val results = timelinePhotoRepository.uploadPhotos(activityId, pending.map { it.toUpload() })
        val timelineIds = results.mapNotNull { upload ->
            upload.result.getOrNull()?.let { upload.clientId to it }
        }.toMap()
        _photos.update { photos ->
            photos.map { photo -> timelineIds[photo.id]?.let { photo.copy(timelineId = it) } ?: photo }
        }
        return pending.size - timelineIds.size
    }

    override suspend fun clear(): Result<Unit> = suspendRunCatching {
        mutex.withLock {
            _photos.value.map { it.activityId }.distinct().forEach { timelinePhotoRepository.clearUploadState(it) }
            _photos.value = emptyList()
            localDataSource.clear()
        }
    }
}

private fun WorkoutPhoto.toUpload() = TimelinePhotoUpload(
    clientId = id,
    file = File(filePath),
    capturedAtEpochMillis = takenAt.toEpochMilli(),
    title = title,
    trackPointIndex = point.pointIndex,
    longitude = point.longitude,
    latitude = point.latitude,
    elevation = point.elevation.toServerElevation(),
    pointIndex = point.pointIndex,
    status = TimelinePhotoStatus.SUCCESSFUL_CREATED,
)
