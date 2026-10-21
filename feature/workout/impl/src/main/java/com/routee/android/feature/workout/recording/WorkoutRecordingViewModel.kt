package com.routee.android.feature.workout.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class WorkoutRecordingViewModel @Inject constructor() : ViewModel() {
    // TODO 위치 기록, 시간, 사진 개수 연결
    private val _uiState = MutableStateFlow(WorkoutRecordingUiState())
    val uiState: StateFlow<WorkoutRecordingUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WorkoutRecordingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onCountdownFinish() = updateMode(WorkoutMode.RECORDING)

    // TODO 기록 일시정지
    fun onStopClick() = updateMode(WorkoutMode.PAUSED)

    // TODO 기록 재개
    fun onRestartClick() = updateMode(WorkoutMode.RECORDING)

    // TODO 기록 종료
    fun onEndLongPress() = updateMode(WorkoutMode.FINISHING)

    fun onFinishAnimationEnd() {
        viewModelScope.launch { _sideEffect.send(WorkoutRecordingSideEffect.NavigateToTimeline) }
    }

    private fun updateMode(mode: WorkoutMode) {
        _uiState.update { it.copy(mode = mode) }
    }
}
