package com.routee.android.feature.timeline.main

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.photo.GalleryImageStore
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
    private val recordSaver: TimelineRecordSaver,
    private val archiveDetailRepository: ArchiveDetailRepository,
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
        title = titleState.text.toString().trim().takeIf { it.isNotEmpty() && it != defaultTitle },
        routePoints = points.map { it.name.text.toString().trim() }.filter(String::isNotEmpty),
    )

    suspend fun save(draft: TimelineDraft): TimelineSaveResult = recordSaver.save(activityId, draft)

    suspend fun deleteRecord(): Result<Unit> =
        archiveDetailRepository.deleteActivity(activityId).onFailure { Timber.e(it, "기록 삭제 실패") }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<TimelineNavKey, TimelineViewModel>
}
