package com.routee.android.feature.workout.recording

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.workout.api.WorkoutRecordingNavKey
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import com.routee.android.feature.workout.recording.model.toLocation
import com.routee.android.feature.workout.recording.model.toMode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import kotlin.math.roundToInt
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val DURATION_TICK_MILLIS = 1_000L

@HiltViewModel(assistedFactory = WorkoutRecordingViewModel.Factory::class)
internal class WorkoutRecordingViewModel @AssistedInject constructor(
    @Assisted private val key: WorkoutRecordingNavKey,
    private val trackingRepository: WorkoutTrackingRepository,
    locationRepository: LocationRepository,
    private val clock: Clock,
) : ViewModel() {
    // TODO 사진 개수 연결
    private val _uiState = MutableStateFlow(WorkoutRecordingUiState())
    val uiState: StateFlow<WorkoutRecordingUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WorkoutRecordingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    val myLocation: Flow<Location> = locationRepository.observeLocations().map { it.toLocation() }

    private var preparing: Job? = null
    private var isStartRequested = false

    init {
        prepareRecording()
        observeTracking()
        updateDurationEverySecond()
    }

    /** 카운트다운이 끝나면 기기 기록 시작, 실패하면 운동 메인으로 */
    fun onCountdownFinish() {
        if (isStartRequested) return
        isStartRequested = true
        viewModelScope.launch {
            preparing?.join()
            val start = key.start ?: return@launch
            if (trackingRepository.state.value.record?.isTracking == true) return@launch
            trackingRepository.start(
                workout = StartedWorkout(activityId = start.activityId, title = start.title),
                type = WorkoutType.HIKING,
                startedAt = Instant.ofEpochMilli(start.startedAtEpochMillis),
            ).onSuccess {
                _uiState.update { it.copy(mode = WorkoutMode.RECORDING) }
                _sideEffect.send(WorkoutRecordingSideEffect.StartTrackingService)
            }.onFailure {
                Timber.e(it, "운동 기록 시작 실패")
                _sideEffect.send(WorkoutRecordingSideEffect.NavigateBack)
            }
        }
    }

    fun onStopClick() {
        viewModelScope.launch { trackingRepository.pause().onFailure { Timber.e(it, "일시정지 실패") } }
    }

    fun onRestartClick() {
        viewModelScope.launch { trackingRepository.resume().onFailure { Timber.e(it, "다시 시작 실패") } }
    }

    // TODO 기록 종료 (PENDING_UPLOAD 저장, 지도 캡처, 완료 API)
    fun onEndLongPress() {
        _uiState.update { it.copy(mode = WorkoutMode.FINISHING) }
    }

    fun onFinishAnimationEnd() {
        viewModelScope.launch { _sideEffect.send(WorkoutRecordingSideEffect.NavigateToTimeline) }
    }

    private fun prepareRecording() {
        preparing = viewModelScope.launch {
            trackingRepository.restore().onFailure { Timber.e(it, "운동 기록 복원 실패") }
            val record = trackingRepository.state.value.record
            when {
                record?.isTracking == true -> continueRecording(record)
                key.start == null -> _sideEffect.send(WorkoutRecordingSideEffect.NavigateBack)
            }
        }
    }

    private fun continueRecording(record: WorkoutRecord) {
        _uiState.update { it.copy(mode = record.status.toMode() ?: it.mode) }
        _sideEffect.trySend(WorkoutRecordingSideEffect.StartTrackingService)
    }

    private fun observeTracking() {
        viewModelScope.launch { trackingRepository.state.collect(::applyTracking) }
    }

    private fun updateDurationEverySecond() {
        viewModelScope.launch {
            while (true) {
                updateDuration()
                delay(DURATION_TICK_MILLIS)
            }
        }
    }

    private fun applyTracking(state: WorkoutTrackingState) {
        val record = state.record ?: return
        _uiState.update {
            it.copy(
                mode = if (it.mode.isTracking) record.status.toMode() ?: it.mode else it.mode,
                distanceMeters = record.distanceMeters.roundToInt(),
                durationSec = record.durationSecondsAt(clock.instant()),
                maxElevationMeters = record.maxElevationMeters?.roundToInt() ?: 0,
                route = state.trackPoints.map { point -> MapPoint(point.latitude, point.longitude) }.toImmutableList(),
            )
        }
    }

    private fun updateDuration() {
        val record = trackingRepository.state.value.record ?: return
        _uiState.update { it.copy(durationSec = record.durationSecondsAt(clock.instant())) }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<WorkoutRecordingNavKey, WorkoutRecordingViewModel>
}
