package com.routee.android.feature.workout.main

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.permission.repository.PermissionRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.location.ObserveCurrentAddressUseCase
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.main.model.WorkoutMainUiState
import com.routee.android.feature.workout.recording.model.observeLocationsFromLast
import com.routee.android.feature.workout.recording.model.toLocation
import com.routee.android.feature.workout.recording.model.toMapPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class WorkoutMainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackingRepository: WorkoutTrackingRepository,
    private val locationRepository: LocationRepository,
    private val permissionRepository: PermissionRepository,
    private val observeCurrentAddress: ObserveCurrentAddressUseCase,
) : ViewModel() {
    // TODO 저장 확인 못 한 기록 다시 보내기, 복구 안내
    private val _uiState = MutableStateFlow(WorkoutMainUiState())
    val uiState: StateFlow<WorkoutMainUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WorkoutMainSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    val myLocation: Flow<Location> = locationRepository.observeLocationsFromLast()
        .map { it.toLocation() }
        .catch { Timber.e(it, "내 위치 수신 실패") }

    // 위치 권한을 요청한 곳. 권한 팝업 중 프로세스가 종료돼도 결과를 처리하게 저장
    private var permissionRequest: PermissionRequest
        get() = savedStateHandle[KEY_PERMISSION_REQUEST] ?: PermissionRequest.NONE
        set(value) {
            savedStateHandle[KEY_PERMISSION_REQUEST] = value
        }
    private var isPermissionChecked = false
    private var isCameraMoved = false
    private var addressJob: Job? = null

    init {
        openRecordingIfTracking()
    }

    fun onLocationPermissionChecked(permission: LocationPermission) {
        _uiState.update { it.copy(locationPermission = permission) }
        if (!isPermissionChecked) {
            isPermissionChecked = true
            if (permission == LocationPermission.DENIED) requestLocationPermissionOnce()
        }
        if (permission == LocationPermission.GRANTED) {
            moveCameraToMyLocationOnce()
            observeAddress()
        } else {
            stopObservingAddress()
        }
    }

    fun onRecordClick() {
        if (uiState.value.locationPermission == LocationPermission.GRANTED) {
            viewModelScope.launch { _sideEffect.send(WorkoutMainSideEffect.NavigateToRecording) }
        } else {
            requestLocationPermission(PermissionRequest.RECORD)
        }
    }

    fun onMyLocationClick() {
        if (uiState.value.locationPermission == LocationPermission.GRANTED) {
            viewModelScope.launch { _sideEffect.send(WorkoutMainSideEffect.FollowMyLocation(from = null)) }
        } else {
            requestLocationPermission(PermissionRequest.MY_LOCATION)
        }
    }

    /** 시스템 권한 팝업 결과. 기록에서 요청해 허용하면 기록 화면으로, 탭해서 요청했는데 거부하거나 대략적인 위치면 안내 모달 */
    fun onLocationPermissionResult(permission: LocationPermission) {
        val request = permissionRequest
        permissionRequest = PermissionRequest.NONE
        onLocationPermissionChecked(permission)
        when {
            permission == LocationPermission.GRANTED -> if (request == PermissionRequest.RECORD) onRecordClick()

            request == PermissionRequest.RECORD || request == PermissionRequest.MY_LOCATION -> {
                showLocationPermissionDialog()
            }

            // 첫 진입이라도 대략적인 위치면 바로 안내
            permission == LocationPermission.APPROXIMATE -> showLocationPermissionDialog()
        }
    }

    fun onScreenPaused() {
        stopObservingAddress()
    }

    private fun showLocationPermissionDialog() {
        _uiState.update { it.copy(isLocationPermissionDialogVisible = true) }
    }

    fun onLocationPermissionDialogDismiss() {
        _uiState.update { it.copy(isLocationPermissionDialogVisible = false) }
    }

    private fun openRecordingIfTracking() {
        viewModelScope.launch {
            trackingRepository.restore().onFailure { Timber.e(it, "운동 기록 복원 실패") }
            if (trackingRepository.state.value.record?.isTracking == true) {
                _sideEffect.send(WorkoutMainSideEffect.NavigateToRecording)
            }
        }
    }

    // 설치 후 첫 진입에만 시스템 팝업. 거부 안내 모달은 운동 기록, 내 위치를 누를 때만
    private fun requestLocationPermissionOnce() {
        viewModelScope.launch {
            val isRequested = permissionRepository.getLocationPermissionRequested().getOrDefault(true)
            if (isRequested) return@launch
            permissionRepository.saveLocationPermissionRequested()
            requestLocationPermission(PermissionRequest.FIRST_ENTRY)
        }
    }

    private fun requestLocationPermission(request: PermissionRequest) {
        permissionRequest = request
        viewModelScope.launch { _sideEffect.send(WorkoutMainSideEffect.RequestLocationPermission) }
    }

    private fun moveCameraToMyLocationOnce() {
        if (isCameraMoved) return
        isCameraMoved = true
        viewModelScope.launch {
            val lastLocation = locationRepository.getLastLocation()
                .onFailure { Timber.e(it, "마지막 위치 조회 실패") }
                .getOrNull()
            _sideEffect.send(WorkoutMainSideEffect.FollowMyLocation(from = lastLocation?.toMapPoint()))
        }
    }

    private fun observeAddress() {
        if (addressJob?.isActive == true) return
        addressJob = observeCurrentAddress(onAddressFailure = { Timber.w(it, "주소 조회 실패") })
            .catch { Timber.e(it, "내 위치 수신 실패") }
            .onEach { address -> _uiState.update { it.copy(address = address) } }
            .launchIn(viewModelScope)
    }

    private fun stopObservingAddress() {
        addressJob?.cancel()
        addressJob = null
    }
}

private const val KEY_PERMISSION_REQUEST = "permission_request"

private enum class PermissionRequest { NONE, FIRST_ENTRY, MY_LOCATION, RECORD }
