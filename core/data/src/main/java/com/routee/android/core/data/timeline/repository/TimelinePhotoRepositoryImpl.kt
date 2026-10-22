package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.model.TimelinePhotoUploadResult
import com.routee.android.core.data.timeline.remote.dto.request.CreateTimelineRequest
import com.routee.android.core.data.timeline.remote.dto.request.TimelineLocationRequest
import com.routee.android.core.data.timeline.remote.service.TimelinePhotoService
import com.routee.android.core.data.upload.PresignedImageUploader
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

private const val MAX_CONCURRENT_PHOTO_UPLOADS = 3
private val TimelineDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

internal class TimelinePhotoRepositoryImpl @Inject constructor(
    private val service: TimelinePhotoService,
    private val uploader: PresignedImageUploader,
    private val apiResponseHandler: ApiResponseHandler,
) : TimelinePhotoRepository {
    private val batchMutex = Mutex()
    private val stateMutex = Mutex()
    private val uploadSemaphore = Semaphore(MAX_CONCURRENT_PHOTO_UPLOADS)
    private val registered = mutableMapOf<Pair<Long, String>, Long>()
    private val uploaded = mutableMapOf<Pair<Long, String>, String>()

    override suspend fun uploadPhotos(
        activityId: Long,
        photos: List<TimelinePhotoUpload>,
    ): List<TimelinePhotoUploadResult> = batchMutex.withLock {
        coroutineScope {
            photos.map { photo ->
                async {
                    uploadSemaphore.withPermit { uploadPhotoOrCached(activityId, photo) }
                }
            }.awaitAll()
        }
    }

    private suspend fun uploadPhotoOrCached(
        activityId: Long,
        photo: TimelinePhotoUpload,
    ): TimelinePhotoUploadResult {
        val key = activityId to photo.clientId
        val cached = stateMutex.withLock { registered[key] }
        val result = cached?.let(Result.Companion::success) ?: uploadPhoto(activityId, photo)
        result.getOrNull()?.let { timelineId -> stateMutex.withLock { registered[key] = timelineId } }
        return TimelinePhotoUploadResult(photo.clientId, result)
    }

    override suspend fun clearUploadState(activityId: Long) = batchMutex.withLock {
        stateMutex.withLock {
            registered.keys.removeAll { it.first == activityId }
            uploaded.keys.removeAll { it.first == activityId }
        }
        Unit
    }

    private suspend fun uploadPhoto(activityId: Long, photo: TimelinePhotoUpload): Result<Long> {
        if (!photo.file.isFile) return Result.failure(FileNotFoundException(photo.file.name))
        val key = activityId to photo.clientId
        val objectKey = stateMutex.withLock { uploaded[key] } ?: run {
            val upload = apiResponseHandler.safeApiCall {
                service.createImageUrl(activityId, ImageUrlRequest(photo.file.name))
            }.getOrElse { return Result.failure(it) }

            apiResponseHandler.safeUnitApiCall {
                uploader.upload(upload.presignedUrl, photo.file)
            }.exceptionOrNull()?.let { return Result.failure(it) }
            stateMutex.withLock { uploaded[key] = upload.objectKey }
            upload.objectKey
        }

        val result = apiResponseHandler.safeApiCall {
            service.createTimeline(
                activityId,
                CreateTimelineRequest(
                    title = photo.title,
                    timelineImageObjectKey = objectKey,
                    createdAt = Instant.ofEpochMilli(photo.capturedAtEpochMillis)
                        .atZone(ZoneId.systemDefault()).toLocalDateTime().format(TimelineDateTimeFormatter),
                    trackPointIndex = photo.trackPointIndex,
                    location = TimelineLocationRequest(
                        longitude = photo.longitude,
                        latitude = photo.latitude,
                        elevation = photo.elevation,
                        pointIndex = photo.pointIndex,
                    ),
                    status = photo.status.name,
                ),
            )
        }.map { it.timelineId }
        if (result.isSuccess) stateMutex.withLock { uploaded.remove(key) }
        return result
    }
}
