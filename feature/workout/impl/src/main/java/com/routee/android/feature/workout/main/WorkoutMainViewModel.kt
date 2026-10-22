package com.routee.android.feature.workout.main

import androidx.lifecycle.ViewModel
import com.routee.android.feature.workout.main.model.WorkoutMainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
internal class WorkoutMainViewModel @Inject constructor() : ViewModel() {
    // TODO 위치 권한 확인, 내 위치 주소 연결
    // TODO 기기에 남은 운동 기록 확인 (복구 안내, 저장 확인 못 한 기록 다시 보내기)
    private val _uiState = MutableStateFlow(WorkoutMainUiState(address = "영등포구 의사당대로 1"))
    val uiState: StateFlow<WorkoutMainUiState> = _uiState.asStateFlow()
}
