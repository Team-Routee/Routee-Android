package com.routee.android.feature.workout.recording

import androidx.lifecycle.ViewModel
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
internal class WorkoutRecordingViewModel @Inject constructor() : ViewModel() {
    // TODO 위치 기록, 시간, 사진 개수 연결
    private val _uiState = MutableStateFlow(WorkoutRecordingUiState())
    val uiState: StateFlow<WorkoutRecordingUiState> = _uiState.asStateFlow()
}
