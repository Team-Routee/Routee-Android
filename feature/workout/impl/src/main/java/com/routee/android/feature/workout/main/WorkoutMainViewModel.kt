package com.routee.android.feature.workout.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.feature.workout.api.WorkoutStart
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.main.model.WorkoutMainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class WorkoutMainViewModel @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val remoteRepository: WorkoutRemoteRepository,
    private val clock: Clock,
) : ViewModel() {
    // TODO 내 위치 주소 연결
    // TODO 저장 확인 못 한 기록 다시 보내기, 복구 안내
    private val _uiState = MutableStateFlow(WorkoutMainUiState(address = "영등포구 의사당대로 1"))
    val uiState: StateFlow<WorkoutMainUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WorkoutMainSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        openRecordingIfTracking()
    }

    /** 위치 권한이 있을 때 운동 기록 시작, 시작 API가 성공하면 기록 화면으로 */
    fun onRecordStart() {
        if (_uiState.value.isStarting) return
        _uiState.update { it.copy(isStarting = true) }
        val startedAt = clock.instant()
        viewModelScope.launch {
            remoteRepository.createWorkout(WorkoutType.HIKING, startedAt)
                .onSuccess { workout ->
                    val start = WorkoutStart(workout.activityId, workout.title, startedAt.toEpochMilli())
                    _sideEffect.send(WorkoutMainSideEffect.NavigateToRecording(start))
                }.onFailure {
                    Timber.e(it, "운동 시작 API 실패")
                    _sideEffect.send(WorkoutMainSideEffect.StartFailed)
                }
            _uiState.update { it.copy(isStarting = false) }
        }
    }

    fun onLocationPermissionChecked(permission: LocationPermission) {
        _uiState.update { it.copy(locationPermission = permission) }
    }

    fun showLocationPermissionDialog() {
        _uiState.update { it.copy(isLocationPermissionDialogVisible = true) }
    }

    fun onLocationPermissionDialogDismiss() {
        _uiState.update { it.copy(isLocationPermissionDialogVisible = false) }
    }

    private fun openRecordingIfTracking() {
        viewModelScope.launch {
            trackingRepository.restore().onFailure { Timber.e(it, "운동 기록 복원 실패") }
            if (trackingRepository.state.value.record?.isTracking == true) {
                _sideEffect.send(WorkoutMainSideEffect.NavigateToRecording())
            }
        }
    }
}
