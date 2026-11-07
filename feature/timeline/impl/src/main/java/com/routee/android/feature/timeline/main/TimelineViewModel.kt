package com.routee.android.feature.timeline.main

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.photo.GalleryImageStore
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.timeline.GetTimelinePhotosUseCase
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.core.ui.UiState
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.component.RoutePoint
import com.routee.android.feature.timeline.main.model.TimelineDraft
import com.routee.android.feature.timeline.main.model.TimelineRequestResult
import com.routee.android.feature.timeline.main.model.TimelineSideEffect
import com.routee.android.feature.timeline.main.model.TimelineTrack
import com.routee.android.feature.timeline.main.model.TimelineUiState
import com.routee.android.feature.timeline.main.model.toRequestFailure
import com.routee.android.feature.timeline.main.model.toTimelineTrack
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MAX_ROUTE_POINT_COUNT = 20

@HiltViewModel(assistedFactory = TimelineViewModel.Factory::class)
internal class TimelineViewModel @AssistedInject constructor(
    @Assisted private val key: TimelineNavKey,
    private val galleryImageStore: GalleryImageStore,
    private val recordSaver: TimelineRecordSaver,
    private val archiveDetailRepository: ArchiveDetailRepository,
    private val recapRepository: RecapRepository,
    private val workoutPhotoRepository: WorkoutPhotoRepository,
    private val workoutTrackingRepository: WorkoutTrackingRepository,
    private val getTimelinePhotos: GetTimelinePhotosUseCase,
) : ViewModel() {
    val activityId = key.activityId
    val endedAtEpochMillis = key.endedAtEpochMillis
    val titleState = TextFieldState()
    val points = mutableStateListOf<RoutePoint>()

    private val _uiState = MutableStateFlow(
        TimelineUiState(
            distance = String.format(Locale.US, "%.2f", key.distanceMeters / 1000.0),
            duration = String.format(Locale.US, "%02d:%02d", key.durationSeconds / 3600, key.durationSeconds / 60 % 60),
            maxElevation = key.maxElevationMeters.toString(),
        ),
    )
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = Channel<TimelineSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var nextPointId = 0L

    init {
        loadTrack()
        loadPhotos()
    }

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

    suspend fun save(draft: TimelineDraft): TimelineRequestResult = recordSaver.save(activityId, draft)

    suspend fun deleteRecord(): TimelineRequestResult = archiveDetailRepository.deleteActivity(activityId).fold(
        onSuccess = { TimelineRequestResult.Success },
        onFailure = {
            Timber.e(it, "기록 삭제 실패")
            it.toRequestFailure()
        },
    )

    // 종료 때 저장한 경로가 기기에 있으면 서버를 부르지 않음
    private fun loadTrack() {
        val deviceRoute = workoutTrackingRepository.getSavedTrackPoints(activityId)
        if (deviceRoute != null) {
            showTrack(deviceRoute.toTimelineTrack(workoutPhotoRepository.photos.value, activityId))
            return
        }
        viewModelScope.launch {
            recapRepository.getActivityTrack(activityId)
                .onSuccess { track -> showTrack(track.toTimelineTrack()) }
                .onFailure { error ->
                    Timber.e(error, "경로 조회 실패")
                    _uiState.update { it.copy(track = UiState.Failure(error)) }
                    _sideEffect.send(TimelineSideEffect.TrackLoadFailed(error.toRequestFailure()))
                }
        }
    }

    private fun showTrack(track: TimelineTrack) {
        _uiState.update { it.copy(track = UiState.Success(track)) }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            // TODO P1 기획: 사진 목록 조회 실패 안내 (지금은 사진 섹션, 나의 루트 초기 목록 없이 진행)
            getTimelinePhotos(activityId)
                .onSuccess(::showPhotos)
                .onFailure { error ->
                    Timber.e(error, "사진 조회 실패")
                    _uiState.update { it.copy(photos = UiState.Failure(error)) }
                }
        }
    }

    private fun showPhotos(photos: List<ArchivePhoto>) {
        _uiState.update { it.copy(photos = UiState.Success(photos)) }
        if (points.isNotEmpty()) return
        photos.mapNotNull(ArchivePhoto::location)
            .take(MAX_ROUTE_POINT_COUNT)
            .forEach { name -> points.add(RoutePoint(nextPointId++, TextFieldState(name))) }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<TimelineNavKey, TimelineViewModel>
}
