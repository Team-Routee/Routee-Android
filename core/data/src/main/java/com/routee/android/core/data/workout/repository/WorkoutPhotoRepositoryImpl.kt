package com.routee.android.core.data.workout.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.recap.model.TimelineMarker
import com.routee.android.core.data.recap.repository.RecapRepository
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val recapRepository: RecapRepository,
    private val archiveDetailRepository: ArchiveDetailRepository,
) : WorkoutPhotoRepository {
    private val _photos = MutableStateFlow<List<WorkoutPhoto>>(emptyList())
    override val photos: StateFlow<List<WorkoutPhoto>> = _photos.asStateFlow()

    // 서버에서 지울 사진 (activityId, timelineId)
    private val removedTimelines = MutableStateFlow<Set<Pair<Long, Long>>>(emptySet())

    // 기기에서 지울 사진 파일, 올리는 중일 수 있어 동기화 때 지움
    private val removedFiles = MutableStateFlow<Set<String>>(emptySet())
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
            clearOthers(activityId)
            _photos.value += photo
        }
        photo
    }

    // 좌표는 경로 조회의 마커, 장소명과 큰 이미지는 타임라인 사진 목록에서. 목록을 못 받으면 썸네일과 빈 장소명
    override suspend fun restore(activityId: Long): Result<Unit> = suspendRunCatching {
        if (_photos.value.any { it.activityId == activityId }) return@suspendRunCatching
        val photos = coroutineScope {
            val track = async { recapRepository.getActivityTrack(activityId) }
            val timelinePhotos = async { archiveDetailRepository.getPhotos(activityId) }
            val photoById = timelinePhotos.await().getOrNull().orEmpty().associateBy { it.id }
            track.await().getOrThrow().timelineMarkers.map { it.toPhoto(activityId, photoById[it.timelineId]) }
        }
        mutex.withLock {
            if (_photos.value.any { it.activityId == activityId }) return@withLock
            clearOthers(activityId)
            _photos.value = photos
        }
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
        removed?.let { photo ->
            photo.filePath?.let { path -> removedFiles.update { it + path } }
            photo.timelineId?.let { markRemoved(photo.activityId, it) }
        }
    }

    override suspend fun syncPhotos(activityId: Long): Int = syncMutex.withLock {
        deleteRemovedFiles()
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
            removedFiles.value = emptySet()
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

    private suspend fun deleteRemovedFiles() {
        val paths = removedFiles.value
        paths.forEach { localDataSource.delete(it) }
        removedFiles.update { it - paths }
    }

    private suspend fun deleteRemoved(activityId: Long) {
        removedTimelines.value.filter { it.first == activityId }.forEach { removed ->
            timelinePhotoRepository.deletePhoto(activityId, removed.second).onSuccess {
                removedTimelines.update { it - removed }
            }
        }
    }

    // 이전 기록에서 남은 사진, 업로드 상태, 지울 사진 정리
    private suspend fun clearOthers(activityId: Long) {
        val others = _photos.value.map { it.activityId }.distinct().filter { it != activityId }
        if (others.isEmpty()) return
        localDataSource.deleteOthers(activityId)
        others.forEach { timelinePhotoRepository.clearUploadState(it) }
        removedTimelines.update { removed -> removed.filterTo(mutableSetOf()) { it.first == activityId } }
        _photos.update { photos -> photos.filter { it.activityId == activityId } }
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
    file = File(checkNotNull(filePath) { "올리지 않은 사진은 기기 파일이 있음" }),
    capturedAtEpochMillis = takenAt.toEpochMilli(),
    title = title,
    trackPointIndex = point.pointIndex,
    longitude = point.longitude,
    latitude = point.latitude,
    elevation = point.elevation.toServerElevation(),
    pointIndex = point.pointIndex,
    status = TimelinePhotoStatus.SUCCESSFUL_CREATED,
)

// 기기 파일 없이 서버 주소로 표시, 경로 좌표는 마커 위치로
private fun TimelineMarker.toPhoto(
    activityId: Long,
    timelinePhoto: ArchivePhoto?,
): WorkoutPhoto {
    val takenAt = timelinePhoto?.createdAt ?: Instant.EPOCH
    return WorkoutPhoto(
        id = "timeline-$timelineId",
        activityId = activityId,
        filePath = null,
        point = TrackPoint(
            pointIndex = pointIndex,
            latitude = latitude,
            longitude = longitude,
            elevation = 0.0,
            recordedAt = takenAt,
        ),
        takenAt = takenAt,
        title = timelinePhoto?.location.orEmpty(),
        timelineId = timelineId,
        serverTitle = timelinePhoto?.location.orEmpty(),
        serverImageUrl = timelinePhoto?.imageUrl ?: thumbnailUrl,
    )
}
