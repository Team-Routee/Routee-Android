package com.routee.android.feature.workout.main

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.permission.repository.PermissionRepository
import com.routee.android.core.data.workout.model.UnfinishedWorkout
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.location.ObserveCurrentAddressUseCase
import com.routee.android.core.domain.workout.DiscardWorkoutUseCase
import com.routee.android.core.domain.workout.GetUnfinishedWorkoutUseCase
import com.routee.android.core.domain.workout.SaveFinishedWorkoutUseCase
import com.routee.android.core.domain.workout.WorkoutSaveResult
import com.routee.android.feature.workout.main.model.LocationPermission
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.main.model.WorkoutMainUiState
import com.routee.android.feature.workout.main.model.toRecoveryUiModel
import com.routee.android.feature.workout.model.observeLocationsFromLast
import com.routee.android.feature.workout.model.toLocation
import com.routee.android.feature.workout.model.toMapPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
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
    private val getUnfinishedWorkout: GetUnfinishedWorkoutUseCase,
    private val saveFinishedWorkout: SaveFinishedWorkoutUseCase,
    private val discardWorkout: DiscardWorkoutUseCase,
) : ViewModel() {
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

    // 복구 모달에서 [이어서 기록]을 눌러 권한 흐름 중. 권한 팝업 중 프로세스가 종료돼도 새 기록을 만들지 않게 저장
    private var isContinuingRecovery: Boolean
        get() = savedStateHandle[KEY_CONTINUING_RECOVERY] ?: false
        set(value) {
            savedStateHandle[KEY_CONTINUING_RECOVERY] = value
        }
    private var isPermissionChecked = false
    private var isCameraMoved = false
    private var addressJob: Job? = null

    init {
        checkUnfinishedWorkout()
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

    // TODO P1 기획: 알림 권한 요청 시점
    // 운동 기록을 누를 때마다 위치 권한 다음에 알림 권한 요청, 두 번 거부하면 시스템이 팝업을 막음
    fun onRecordClick() {
        if (uiState.value.locationPermission == LocationPermission.GRANTED) {
            viewModelScope.launch { _sideEffect.send(WorkoutMainSideEffect.RequestNotificationPermission) }
        } else {
            requestLocationPermission(PermissionRequest.RECORD)
        }
    }

    /** 알림 권한 팝업 결과. 거부해도 안내 없이 기록 화면으로, 복구 중이면 이전 기록을 불러와 이어서 기록 */
    fun onNotificationPermissionResult() {
        if (isContinuingRecovery) {
            continueRecovery()
        } else {
            viewModelScope.launch { _sideEffect.send(WorkoutMainSideEffect.NavigateToRecording) }
        }
    }

    /** 복구 모달 [이어서 기록], 모달을 내리고 [운동 기록]과 같은 권한 흐름 뒤 이어서 기록 */
    fun onRecoveryContinueClick() {
        if (!startRecoveryAction()) return
        isContinuingRecovery = true
        onRecordClick()
    }

    /** 복구 모달 [종료 후 저장], 마지막 기록 시각에 종료하고 경로 전체를 캡처해 저장. 위치 권한은 필요 없음 */
    fun onRecoverySaveClick() {
        if (_uiState.value.isRecoverySaving || _uiState.value.isRecoveryInProgress) return
        _uiState.update { it.copy(recovery = null, isRecoverySaving = true) }
        viewModelScope.launch { finishRecoveredWorkout() }
    }

    /** 복구 모달 X, 저장하지 않고 기기 기록 삭제. 실패하면 모달로 돌아감 */
    fun onRecoveryDiscardClick() {
        if (!startRecoveryAction()) return
        viewModelScope.launch {
            discardWorkout()
                .onSuccess { _uiState.update { it.copy(recovery = null, isRecoveryInProgress = false) } }
                .onFailure {
                    Timber.e(it, "복구하지 않을 기록 삭제 실패")
                    _uiState.update { state -> state.copy(isRecoveryInProgress = false) }
                }
        }
    }

    /** 종료 후 저장, 재전송 지도 캡처를 받아 저장, 캡처 실패면 null */
    fun onRecoveryMapCaptured(jpeg: ByteArray?) {
        if (_uiState.value.recoveryCaptureRoute == null) return
        _uiState.update { it.copy(recoveryCaptureRoute = null) }
        viewModelScope.launch { showRecoverySaveResult(saveFinishedWorkout(jpeg)) }
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

    /** 위치 권한 안내 모달 닫기, 복구 중이었으면 복구 모달로 돌아감 */
    fun onLocationPermissionDialogDismiss() {
        isContinuingRecovery = false
        _uiState.update { it.copy(isLocationPermissionDialogVisible = false, isRecoveryInProgress = false) }
    }

    // 고른 동작이 끝날 때까지 모달을 내려 다른 버튼을 막음, 이미 처리 중이면 false
    private fun startRecoveryAction(): Boolean {
        if (_uiState.value.isRecoveryInProgress || _uiState.value.isRecoverySaving) return false
        _uiState.update { it.copy(isRecoveryInProgress = true) }
        return true
    }

    // 확인과 재전송이 끝날 때까지 운동 기록 비활성화, 새 기록이 남은 기록과 겹치지 않게
    private fun checkUnfinishedWorkout() {
        viewModelScope.launch {
            getUnfinishedWorkout()
                .onSuccess { handleUnfinishedWorkout(it) }
                .onFailure { Timber.e(it, "남은 운동 기록 확인 실패") }
            _uiState.update { it.copy(isCheckingUnfinishedWorkout = false) }
        }
    }

    // 앱이 살아 있으면 기록 화면으로, 꺼졌다 켜졌거나 일시정지 2시간이 넘었으면 복구 모달, 저장 확인 전이면 다시 보냄
    private suspend fun handleUnfinishedWorkout(unfinished: UnfinishedWorkout) {
        when (unfinished) {
            UnfinishedWorkout.None -> Unit

            UnfinishedWorkout.Tracking -> _sideEffect.send(WorkoutMainSideEffect.NavigateToRecording)

            is UnfinishedWorkout.Recoverable -> {
                _uiState.update { it.copy(recovery = unfinished.record.toRecoveryUiModel()) }
            }

            is UnfinishedWorkout.PendingUpload -> resendPendingWorkout()
        }
    }

    // TODO P1 기획: 다시 보낸 결과 토스트, 인터넷이나 서버 오류면 안내 없이 다음 진입 때 다시
    // 지도 키나 캡처 파일이 있으면 그대로 보내고, 없으면 경로 전체를 다시 캡처
    private suspend fun resendPendingWorkout() {
        when (val result = saveFinishedWorkout(mapImage = null)) {
            WorkoutSaveResult.MapImageMissing -> requestRouteCapture()
            WorkoutSaveResult.Failed -> Timber.w("업로드 대기 기록 다시 보내기 실패, 다음 진입 때 다시")
            else -> showSaveResultToast(result)
        }
    }

    private fun continueRecovery() {
        viewModelScope.launch {
            isContinuingRecovery = false
            trackingRepository.recover()
                .onSuccess {
                    _uiState.update { it.copy(recovery = null, isRecoveryInProgress = false) }
                    _sideEffect.send(WorkoutMainSideEffect.NavigateToRecoveredRecording)
                }.onFailure {
                    Timber.e(it, "운동 기록 복구 실패")
                    _uiState.update { state -> state.copy(isRecoveryInProgress = false) }
                    _sideEffect.send(WorkoutMainSideEffect.ShowRecoveryFailed)
                }
        }
    }

    // 꺼져 있던 시간은 빼고 마지막 기록 시각에 종료
    private suspend fun finishRecoveredWorkout() {
        trackingRepository.recover()
            .mapCatching {
                val record = checkNotNull(trackingRepository.state.value.record) { "복구할 기록 없음" }
                trackingRepository.finish(endedAt = record.lastRecordedAt).getOrThrow()
            }.onSuccess {
                requestRouteCapture()
            }.onFailure {
                Timber.e(it, "복구한 기록 종료 실패")
                showRecoverySaveResult(WorkoutSaveResult.Failed)
            }
    }

    // 경로 전체 캡처를 로딩과 함께 요청, 캡처는 onRecoveryMapCaptured로 받음
    private fun requestRouteCapture() {
        val route = trackingRepository.state.value.trackPoints.map { it.toMapPoint() }.toImmutableList()
        _uiState.update { it.copy(isRecoverySaving = true, recoveryCaptureRoute = route) }
    }

    private suspend fun showRecoverySaveResult(result: WorkoutSaveResult) {
        _uiState.update { it.copy(isRecoverySaving = false) }
        showSaveResultToast(result)
        if (_uiState.value.locationPermission == LocationPermission.GRANTED) {
            _sideEffect.send(WorkoutMainSideEffect.FollowMyLocation(from = null))
        }
    }

    // TODO P1 기획: 경로가 2점 미만이라 저장할 수 없을 때도 실패 토스트
    private suspend fun showSaveResultToast(result: WorkoutSaveResult) {
        val isSaved = result == WorkoutSaveResult.Saved || result == WorkoutSaveResult.PhotosMissing
        if (!isSaved) Timber.w("운동 기록 저장 실패: $result")
        _sideEffect.send(
            if (isSaved) WorkoutMainSideEffect.ShowRecoverySaved else WorkoutMainSideEffect.ShowRecoverySaveFailed,
        )
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
private const val KEY_CONTINUING_RECOVERY = "continuing_recovery"

private enum class PermissionRequest { NONE, FIRST_ENTRY, MY_LOCATION, RECORD }
