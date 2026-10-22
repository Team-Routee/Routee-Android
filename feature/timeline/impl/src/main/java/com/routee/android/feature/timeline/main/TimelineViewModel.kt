package com.routee.android.feature.timeline.main

import android.graphics.Bitmap
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.routee.android.core.data.photo.GalleryImageStore
import com.routee.android.core.data.timeline.model.TimelineCompletion
import com.routee.android.core.data.timeline.model.TimelinePhotoStatus
import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.model.TimelineTrackPoint
import com.routee.android.core.data.timeline.repository.TimelineCompletionRepository
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.timeline.repository.TimelinePhotoRepository
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.component.RoutePoint
import com.routee.android.feature.timeline.main.model.TimelineDraft
import com.routee.android.feature.timeline.main.model.TimelineSaveResult
import com.routee.android.feature.timeline.main.model.TimelineUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

private const val MAX_ROUTE_POINT_COUNT = 20

@HiltViewModel(assistedFactory = TimelineViewModel.Factory::class)
internal class TimelineViewModel @AssistedInject constructor(
    @Assisted private val key: TimelineNavKey,
    private val galleryImageStore: GalleryImageStore,
    private val mapImageRepository: TimelineMapImageRepository,
    private val completionRepository: TimelineCompletionRepository,
    private val photoRepository: TimelinePhotoRepository,
) : ViewModel() {
    val activityId = key.activityId
    val endedAtEpochMillis = key.endedAtEpochMillis
    val titleState = TextFieldState()
    val photos = key.photos
    val points = key.photos
        .mapNotNull { photo -> photo.location?.takeIf(String::isNotBlank) }
        .take(MAX_ROUTE_POINT_COUNT)
        .mapIndexed { index, name -> RoutePoint(index.toLong(), TextFieldState(name)) }
        .toMutableStateList()

    private val _uiState = MutableStateFlow(
        TimelineUiState(
            distance = String.format(Locale.US, "%.2f", key.distanceMeters / 1000.0),
            duration = String.format(Locale.US, "%02d:%02d", key.durationSeconds / 3600, key.durationSeconds / 60 % 60),
            maxElevation = key.maxElevationMeters.toString(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    private var nextPointId = (points.maxOfOrNull(RoutePoint::id) ?: -1L) + 1L
    private var uploadedMapKey: String? = null

    suspend fun savePhoto(imageUrl: String, capturedAtEpochMillis: Long): Result<Unit> =
        galleryImageStore.save(imageUrl, capturedAtEpochMillis).onFailure { Timber.e(it) }

    fun onEditTitleClick(defaultTitle: String) {
        if (titleState.text.isEmpty()) titleState.setTextAndPlaceCursorAtEnd(defaultTitle)
        _uiState.update { it.copy(isTitleEditing = true) }
    }

    fun onTitleEditFinish() {
        _uiState.update { it.copy(isTitleEditing = false) }
    }

    fun onMovePoint(from: Int, to: Int) {
        if (from !in points.indices || to !in points.indices || from == to) return
        points.add(to, points.removeAt(from))
    }

    fun onRemovePoint(index: Int) {
        if (index in points.indices) points.removeAt(index)
    }

    fun onAddPoint() {
        if (points.size >= MAX_ROUTE_POINT_COUNT) return
        points.add(RoutePoint(nextPointId++, TextFieldState()))
    }

    fun draft(defaultTitle: String) = TimelineDraft(
        title = titleState.text.toString().trim().ifEmpty { defaultTitle },
        routePoints = points.map { it.name.text.toString().trim() }.filter(String::isNotEmpty),
    )

    suspend fun save(draft: TimelineDraft, bitmap: Bitmap): TimelineSaveResult {
        if (key.routePoints.size < 2) return TimelineSaveResult.InvalidRoute
        if (key.photos.size != key.photoUploads.size || key.durationSeconds <= 0 || key.distanceMeters < 0) {
            return TimelineSaveResult.Failed
        }

        val photoResults = photoRepository.uploadPhotos(
            key.activityId,
            key.photoUploads.map { photo ->
                TimelinePhotoUpload(
                    clientId = photo.clientId,
                    file = File(photo.filePath),
                    capturedAtEpochMillis = photo.capturedAtEpochMillis,
                    title = photo.title.orEmpty(),
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

        val mapKey = uploadedMapKey ?: mapImageRepository.uploadMapImage(key.activityId, bitmap)
            .getOrElse { return TimelineSaveResult.Failed }
            .also { uploadedMapKey = it }
        val completion = TimelineCompletion(
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
        if (completionRepository.complete(completion, mapKey).isFailure) return TimelineSaveResult.Failed

        photoRepository.clearUploadState(key.activityId)
        uploadedMapKey = null
        return TimelineSaveResult.Success
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<TimelineNavKey, TimelineViewModel>
}
