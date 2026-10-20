package com.routee.android.feature.timeline.main

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.routee.android.core.data.timeline.model.TimelineCompletion
import com.routee.android.core.data.timeline.model.TimelinePhotoStatus
import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.model.TimelineTrackPoint
import com.routee.android.core.data.timeline.repository.TimelineCompletionRepository
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.timeline.repository.TimelinePhotoRepository
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.main.model.TimelineDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class TimelineSaveViewModel @Inject constructor(
    private val mapImageRepository: TimelineMapImageRepository,
    private val completionRepository: TimelineCompletionRepository,
    private val photoRepository: TimelinePhotoRepository,
) : ViewModel() {
    private var uploadedMap: Pair<Long, String>? = null

    suspend fun save(key: TimelineNavKey, draft: TimelineDraft, bitmap: Bitmap): TimelineSaveResult {
        if (key.routePoints.size < 2) return TimelineSaveResult.InvalidRoute
        if (key.photos.size != key.photoUploads.size) return TimelineSaveResult.Failed
        if (key.durationSeconds <= 0 || key.distanceMeters < 0) {
            return TimelineSaveResult.Failed
        }

        val photoResults = photoRepository.uploadPhotos(
            key.activityId,
            key.photoUploads.map { photo ->
                TimelinePhotoUpload(
                    clientId = photo.clientId,
                    file = File(photo.filePath),
                    capturedAtEpochMillis = photo.capturedAtEpochMillis,
                    title = photo.title,
                    trackPointIndex = photo.trackPointIndex,
                    longitude = photo.longitude,
                    latitude = photo.latitude,
                    elevation = photo.elevation,
                    pointIndex = photo.pointIndex,
                    status = if (photo.creationInProgress) {
                        TimelinePhotoStatus.CREATION_IN_PROGRESS
                    } else {
                        TimelinePhotoStatus.SUCCESSFUL_CREATED
                    },
                )
            },
        )
        if (photoResults.any { it.result.isFailure }) return TimelineSaveResult.Failed

        val mapKey = uploadedMap?.takeIf { it.first == key.activityId }?.second ?: mapImageRepository
            .uploadMapImage(key.activityId, bitmap)
            .getOrElse { return TimelineSaveResult.Failed }
            .also { uploadedMap = key.activityId to it }

        val input = TimelineCompletion(
            activityId = key.activityId,
            title = draft.title,
            distanceMeters = key.distanceMeters,
            durationSeconds = key.durationSeconds,
            maxElevationMeters = key.maxElevationMeters,
            endedAtEpochMillis = key.endedAtEpochMillis,
            track = key.routePoints.map { point ->
                TimelineTrackPoint(point.latitude, point.longitude, point.elevation, point.pointIndex)
            },
            routeNames = draft.routePoints,
        )
        return if (completionRepository.complete(input, mapKey).isSuccess) {
            TimelineSaveResult.Success
        } else {
            TimelineSaveResult.Failed
        }
    }
}
