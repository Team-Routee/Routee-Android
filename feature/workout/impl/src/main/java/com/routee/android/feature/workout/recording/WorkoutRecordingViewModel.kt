package com.routee.android.feature.workout.recording

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.UnfinishedWorkout
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.workout.AddWorkoutPhotoResult
import com.routee.android.core.domain.workout.AddWorkoutPhotoUseCase
import com.routee.android.core.domain.workout.GetUnfinishedWorkoutUseCase
import com.routee.android.core.domain.workout.SaveFinishedWorkoutUseCase
import com.routee.android.core.domain.workout.WorkoutSaveResult
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.workout.api.WorkoutRecordingNavKey
import com.routee.android.feature.workout.model.observeLocationsFromLast
import com.routee.android.feature.workout.model.toLocation
import com.routee.android.feature.workout.model.toMapPoint
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import com.routee.android.feature.workout.recording.model.WorkoutSaveFailure
import com.routee.android.feature.workout.recording.model.WorkoutSummary
import com.routee.android.feature.workout.recording.model.toMapPhoto
import com.routee.android.feature.workout.recording.model.toMode
import com.routee.android.feature.workout.recording.model.toOpenedPhoto
import com.routee.android.feature.workout.recording.model.toSummary
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.Clock
import kotlin.math.roundToInt
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val getUnfinishedWorkout: GetUnfinishedWorkoutUseCase,
    private val remoteRepository: WorkoutRemoteRepository,
    private val saveFinishedWorkout: SaveFinishedWorkoutUseCase,
    private val addWorkoutPhoto: AddWorkoutPhotoUseCase,
    private val photoRepository: WorkoutPhotoRepository,
    private val locationRepository: LocationRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutRecordingUiState())
    val uiState: StateFlow<WorkoutRecordingUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WorkoutRecordingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    val myLocation: Flow<Location> = locationRepository.observeLocationsFromLast()
        .map { it.toLocation() }
        .catch { Timber.e(it, "내 위치 수신 실패") }

    private val startedAt = clock.instant()
    private var startedWorkout: Deferred<Result<StartedWorkout>>? = null
    private var preparing: Job? = null
    private var summary: WorkoutSummary? = null
    private val saveResult = CompletableDeferred<WorkoutSaveResult>()
    private var isTakingPhoto = false

    // 복구 모달에서 이어서 기록, 카운트다운이 끝나면 불러온 기록을 다시 시작
    private var isResumingRecovered = false

    // 삭제 스낵바가 떠 있는 사진, 스낵바가 닫히면 삭제 확정
    private val pendingDeletePhotoId = MutableStateFlow<String?>(null)

    init {
        moveToLastLocation()
        prepareRecording()
        observeTracking()
        observePhotos()
        updateDurationEverySecond()
        navigateHomeWhenPauseExpired()
    }

    /** 카운트다운이 끝나면 시작 API 결과를 기다려 기록 시작, 시작 API 실패는 카운트다운 중에 바로 알림. 복구면 불러온 기록을 다시 시작 */
    fun onCountdownFinish() {
        viewModelScope.launch {
            preparing?.join()
            if (isResumingRecovered) resumeRecovered() else startCreatedWorkout()
        }
    }

    fun onStopClick() {
        viewModelScope.launch { trackingRepository.pause().onFailure { Timber.e(it, "일시정지 실패") } }
    }

    fun onRestartClick() {
        viewModelScope.launch {
            trackingRepository.resume().onFailure {
                Timber.e(it, "다시 시작 실패")
                _sideEffect.send(WorkoutRecordingSideEffect.ShowResumeFailed)
            }
        }
    }

    /** 카메라 원본을 쓸 파일을 만들어 기본 카메라 실행, 찍는 중이거나 20장이거나 삭제 스낵바가 떠 있으면 무시 */
    fun onCameraClick() {
        if (isTakingPhoto || !_uiState.value.isCameraEnabled || pendingDeletePhotoId.value != null) return
        isTakingPhoto = true
        viewModelScope.launch {
            photoRepository.createCaptureFile()
                .onSuccess { _sideEffect.send(WorkoutRecordingSideEffect.TakePhoto(it.path)) }
                .onFailure {
                    Timber.e(it, "촬영 파일 생성 실패")
                    isTakingPhoto = false
                    _sideEffect.send(WorkoutRecordingSideEffect.ShowPhotoFailed)
                }
        }
    }

    /**
     * 카메라가 끝나면 사진 추가
     *
     * @param isSaved 카메라가 사진을 저장했으면 true, 취소했거나 카메라를 열지 못했으면 false
     */
    fun onPhotoTaken(
        filePath: String,
        isSaved: Boolean,
    ) {
        isTakingPhoto = false
        viewModelScope.launch {
            // 기록 확인을 기다림. 카메라가 떠 있는 동안 앱이 종료됐으면 복구 모달로 가고 사진은 버림
            preparing?.join()
            val file = File(filePath)
            if (!isSaved) {
                photoRepository.discardCaptureFile(file)
                return@launch
            }
            when (addWorkoutPhoto(file)) {
                AddWorkoutPhotoResult.Added -> Unit
                AddWorkoutPhotoResult.NoLocation -> _sideEffect.send(WorkoutRecordingSideEffect.ShowPhotoNoLocation)
                AddWorkoutPhotoResult.Failed -> _sideEffect.send(WorkoutRecordingSideEffect.ShowPhotoFailed)
            }
        }
    }

    /** 기본 카메라 앱이 없어 열지 못함, 원본 파일을 지우고 안내 모달 */
    fun onCameraUnavailable(filePath: String) {
        _uiState.update { it.copy(isCameraUnavailable = true) }
        onPhotoTaken(filePath, isSaved = false)
    }

    fun onCameraUnavailableDialogConfirm() {
        _uiState.update { it.copy(isCameraUnavailable = false) }
    }

    fun onPhotoMarkerClick(photoId: String) {
        val photo = photoRepository.photos.value.find { it.id == photoId } ?: return
        _uiState.update { it.copy(openedPhoto = photo.toOpenedPhoto()) }
    }

    /** 사진 모달 닫기, 장소명은 닫을 때 저장 */
    fun onPhotoDialogClose(title: String) {
        val photo = _uiState.value.openedPhoto ?: return
        _uiState.update { it.copy(openedPhoto = null) }
        if (title == photo.title) return
        photoRepository.updateTitle(photo.id, title)
        syncPhotos()
    }

    /** 사진 모달에서 삭제, 지도와 개수에서 바로 빼고 스낵바가 닫히면 확정 */
    fun onPhotoDeleteClick() {
        val photo = _uiState.value.openedPhoto ?: return
        confirmPhotoDelete()
        pendingDeletePhotoId.value = photo.id
        _uiState.update { it.copy(openedPhoto = null, isPhotoDeletePending = true) }
        _sideEffect.trySend(WorkoutRecordingSideEffect.ShowPhotoDeleted(photo.id))
    }

    /**
     * 삭제 스낵바가 닫히면 확정하거나 되돌림
     *
     * @param isUndone [취소]나 뒤로가기로 닫았으면 true
     */
    fun onPhotoDeleteSnackbarClose(
        photoId: String,
        isUndone: Boolean,
    ) {
        if (pendingDeletePhotoId.value != photoId) return
        if (isUndone) {
            pendingDeletePhotoId.value = null
            _uiState.update { it.copy(isPhotoDeletePending = false) }
        } else {
            confirmPhotoDelete()
        }
    }

    /** 종료, 마무리 애니메이션과 함께 기록을 업로드 대기로 바꾸고 지도 캡처 요청 */
    fun onEndLongPress() {
        if (_uiState.value.mode == WorkoutMode.FINISHING) return
        confirmPhotoDelete()
        _uiState.update { it.copy(mode = WorkoutMode.FINISHING) }
        viewModelScope.launch { finishWorkout() }
    }

    /** 지도 캡처를 받아 저장, 캡처 실패면 null. 화면이 다시 만들어져 또 오면 무시 */
    fun onMapCaptured(jpeg: ByteArray?) {
        if (_uiState.value.captureRoute == null) return
        _uiState.update { it.copy(captureRoute = null) }
        viewModelScope.launch { saveResult.complete(saveFinishedWorkout(jpeg)) }
    }

    /** 마무리 애니메이션이 끝나면 저장 결과를 기다려 타임라인 이동, 실패면 기록 등록 실패 모달 */
    fun onFinishAnimationEnd() {
        viewModelScope.launch {
            val result = saveResult.await()
            val summary = summary
            when {
                result == WorkoutSaveResult.Saved && summary != null ->
                    _sideEffect.send(WorkoutRecordingSideEffect.NavigateToTimeline(summary))

                result == WorkoutSaveResult.PhotosMissing && summary != null -> {
                    Timber.w("종료 때 다시 올려도 빠진 사진이 있음")
                    _sideEffect.send(WorkoutRecordingSideEffect.ShowPhotosMissing)
                    _sideEffect.send(WorkoutRecordingSideEffect.NavigateToTimeline(summary))
                }

                result == WorkoutSaveResult.InvalidRoute ->
                    _uiState.update { it.copy(saveFailure = WorkoutSaveFailure.INVALID_ROUTE) }

                else -> {
                    Timber.w("운동 저장 실패, 기기 기록은 업로드 대기로 남음")
                    _uiState.update { it.copy(saveFailure = WorkoutSaveFailure.NETWORK) }
                }
            }
        }
    }

    fun onSaveFailureDialogConfirm() {
        _uiState.update { it.copy(saveFailure = null) }
        viewModelScope.launch { _sideEffect.send(WorkoutRecordingSideEffect.NavigateToHome) }
    }

    // 스낵바가 결과 없이 닫혀도 화면을 떠날 때 확정
    override fun onCleared() {
        confirmPhotoDelete()
    }

    private suspend fun finishWorkout() {
        trackingRepository.finish().onFailure {
            Timber.e(it, "운동 종료 실패")
            saveResult.complete(WorkoutSaveResult.Failed)
            return
        }
        val state = trackingRepository.state.value
        summary = state.record?.toSummary()
        val route = state.trackPoints.map { it.toMapPoint() }.toImmutableList()
        _uiState.update { it.copy(captureRoute = route) }
    }

    // 첫 위치를 받기 전 기본 위치가 보이지 않게 마지막 위치에서 시작
    private fun moveToLastLocation() {
        viewModelScope.launch {
            val lastLocation = locationRepository.getLastLocation()
                .onFailure { Timber.e(it, "마지막 위치 조회 실패") }
                .getOrNull() ?: return@launch
            _sideEffect.send(WorkoutRecordingSideEffect.MoveToLastLocation(lastLocation.toMapPoint()))
        }
    }

    // 앱이 꺼졌다 켜져 기록 화면이 바로 열리면 운동 메인의 복구 모달로 보냄
    private fun prepareRecording() {
        preparing = viewModelScope.launch {
            val unfinished = getUnfinishedWorkout().getOrElse {
                Timber.e(it, "운동 기록 복원 실패")
                _sideEffect.send(WorkoutRecordingSideEffect.ShowRestoreFailed)
                UnfinishedWorkout.None
            }
            when (unfinished) {
                UnfinishedWorkout.Tracking -> {
                    restorePhotos()
                    if (key.isRecovering) isResumingRecovered = true else continueRecording()
                }

                is UnfinishedWorkout.Recoverable -> _sideEffect.send(WorkoutRecordingSideEffect.NavigateToHome)

                else -> createWorkout()
            }
        }
    }

    // 앱이 다시 켜져 사진 목록이 비었으면 서버에 올린 사진으로 마커와 개수 복구, 복구면 카운트다운 동안
    private fun restorePhotos() {
        val activityId = trackingRepository.state.value.record?.activityId ?: return
        viewModelScope.launch { photoRepository.restore(activityId).onFailure { Timber.e(it, "운동 사진 복구 실패") } }
    }

    private fun continueRecording() {
        val status = trackingRepository.state.value.record?.status ?: return
        _uiState.update { it.copy(mode = status.toMode() ?: it.mode) }
        _sideEffect.trySend(WorkoutRecordingSideEffect.StartTrackingService)
    }

    private suspend fun startCreatedWorkout() {
        val pending = startedWorkout ?: return
        startedWorkout = null
        pending.await().onSuccess { workout ->
            trackingRepository.start(workout, WorkoutType.HIKING, startedAt)
                .onSuccess {
                    _uiState.update { it.copy(mode = WorkoutMode.RECORDING) }
                    _sideEffect.send(WorkoutRecordingSideEffect.StartTrackingService)
                }.onFailure {
                    Timber.e(it, "운동 기록 시작 실패")
                    _sideEffect.send(WorkoutRecordingSideEffect.StartFailed)
                }
        }
    }

    // 일시정지로 불러온 기록을 기록 중으로, 화면이 떠 있을 때 서비스 시작. 기기 저장이 실패하면 정지 화면으로 둠
    private suspend fun resumeRecovered() {
        isResumingRecovered = false
        trackingRepository.resume().onFailure {
            Timber.e(it, "복구한 기록 다시 시작 실패")
            _sideEffect.send(WorkoutRecordingSideEffect.ShowResumeFailed)
        }
        continueRecording()
    }

    private fun createWorkout() {
        startedWorkout = viewModelScope.async {
            remoteRepository.createWorkout(WorkoutType.HIKING, startedAt).onFailure {
                Timber.e(it, "운동 시작 API 실패")
                _sideEffect.send(WorkoutRecordingSideEffect.StartFailed)
            }
        }
    }

    private fun observeTracking() {
        viewModelScope.launch { trackingRepository.state.collect(::applyTracking) }
    }

    // 이전 기록에서 남은 사진, 삭제 스낵바가 떠 있는 사진은 빼고 지금 기록 사진만
    private fun observePhotos() {
        viewModelScope.launch {
            combine(
                trackingRepository.state.map { it.record?.activityId }.distinctUntilChanged(),
                photoRepository.photos,
                pendingDeletePhotoId,
            ) { activityId, photos, pendingDeleteId ->
                photos.filter { it.activityId == activityId && it.id != pendingDeleteId }
            }
                .collect { photos ->
                    _uiState.update { it.copy(photos = photos.map { photo -> photo.toMapPhoto() }.toImmutableList()) }
                }
        }
    }

    private fun confirmPhotoDelete() {
        val photoId = pendingDeletePhotoId.value ?: return
        photoRepository.removePhoto(photoId)
        pendingDeletePhotoId.value = null
        _uiState.update { it.copy(isPhotoDeletePending = false) }
        syncPhotos()
    }

    private fun syncPhotos() {
        val activityId = trackingRepository.state.value.record?.activityId ?: return
        viewModelScope.launch { photoRepository.syncPhotos(activityId) }
    }

    private fun updateDurationEverySecond() {
        viewModelScope.launch {
            while (true) {
                updateDuration()
                delay(DURATION_TICK_MILLIS)
            }
        }
    }

    private fun navigateHomeWhenPauseExpired() {
        viewModelScope.launch {
            while (!isPauseExpired()) delay(DURATION_TICK_MILLIS)
            _sideEffect.send(WorkoutRecordingSideEffect.NavigateToHome)
        }
    }

    private fun isPauseExpired(): Boolean =
        trackingRepository.state.value.record?.isPauseExpiredAt(clock.instant()) == true

    private fun applyTracking(state: WorkoutTrackingState) {
        val record = state.record ?: return
        _uiState.update {
            it.copy(
                mode = if (it.mode.isTracking) record.status.toMode() ?: it.mode else it.mode,
                distanceMeters = record.distanceMeters.roundToInt(),
                durationSec = record.durationSecondsAt(clock.instant()),
                maxElevationMeters = record.maxElevationMeters?.roundToInt() ?: 0,
                route = state.trackPoints.map { it.toMapPoint() }.toImmutableList(),
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
