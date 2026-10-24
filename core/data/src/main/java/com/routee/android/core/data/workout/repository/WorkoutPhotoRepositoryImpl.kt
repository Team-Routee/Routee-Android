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

    // 서버에서 지울 사진 (activityId, timelineId)
    private val removedTimelines = MutableStateFlow<Set<Pair<Long, Long>>>(emptySet())
    private val mutex = Mutex()
    private val syncMutex = Mutex()

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
                removedTimelines.update { removed -> removed.filterTo(mutableSetOf()) { it.first == activityId } }
                _photos.value = listOf(photo)
            } else {
                _photos.value = previous + photo
            }
        }
        photo
    }

    override suspend fun discardCaptureFile(file: File) = localDataSource.delete(file.path)

    override fun updateTitle(
        photoId: String,
        title: String,
    ) {
        _photos.update { photos -> photos.map { if (it.id == photoId) it.copy(title = title) else it } }
    }

    override fun removePhoto(photoId: String) {
        var removed: WorkoutPhoto? = null
        _photos.update { photos ->
            removed = photos.find { it.id == photoId }
            photos.filterNot { it.id == photoId }
        }
        removed?.let { photo -> photo.timelineId?.let { markRemoved(photo.activityId, it) } }
    }

    override suspend fun syncPhotos(activityId: Long): Int = syncMutex.withLock {
        uploadPending(activityId)
        updateTitles(activityId)
        deleteRemoved(activityId)
        _photos.value.count { it.activityId == activityId && it.timelineId == null }
    }

    override suspend fun clear(): Result<Unit> = suspendRunCatching {
        mutex.withLock {
            _photos.value.map { it.activityId }.distinct().forEach { timelinePhotoRepository.clearUploadState(it) }
            _photos.value = emptyList()
            removedTimelines.value = emptySet()
            localDataSource.clear()
        }
    }

    private suspend fun uploadPending(activityId: Long) {
        val pending = _photos.value.filter { it.activityId == activityId && it.timelineId == null }
        if (pending.isEmpty()) return
        val sentTitles = pending.associate { it.id to it.title }
        val timelineIds = timelinePhotoRepository.uploadPhotos(activityId, pending.map { it.toUpload() })
            .mapNotNull { upload -> upload.result.getOrNull()?.let { upload.clientId to it } }
            .toMap()
        _photos.update { photos ->
            photos.map { photo ->
                timelineIds[photo.id]?.let { photo.copy(timelineId = it, serverTitle = sentTitles[photo.id]) } ?: photo
            }
        }
        // 올리는 동안 지운 사진은 서버에서도 지움
        val remaining = _photos.value.map { it.id }.toSet()
        timelineIds.filterKeys { it !in remaining }.values.forEach { markRemoved(activityId, it) }
    }

    // 서버는 빈 장소명으로 수정하지 않음
    private suspend fun updateTitles(activityId: Long) {
        _photos.value
            .filter { it.activityId == activityId && it.title.isNotBlank() && it.title != it.serverTitle }
            .forEach { photo ->
                val timelineId = photo.timelineId ?: return@forEach
                timelinePhotoRepository.updateTitle(activityId, timelineId, photo.title).onSuccess {
                    _photos.update { photos ->
                        photos.map { if (it.id == photo.id) it.copy(serverTitle = photo.title) else it }
                    }
                }
            }
    }

    private suspend fun deleteRemoved(activityId: Long) {
        removedTimelines.value.filter { it.first == activityId }.forEach { removed ->
            timelinePhotoRepository.deletePhoto(activityId, removed.second).onSuccess {
                removedTimelines.update { it - removed }
            }
        }
    }

    private fun markRemoved(
        activityId: Long,
        timelineId: Long,
    ) {
        removedTimelines.update { it + (activityId to timelineId) }
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
