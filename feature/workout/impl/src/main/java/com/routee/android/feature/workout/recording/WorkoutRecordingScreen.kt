package com.routee.android.feature.workout.recording

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.location.Location
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.data.upload.UPLOAD_JPEG_QUALITY
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.dialog.RouteeOneButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.message.RouteeMessageResult
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeMapState
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.mapLogoAbove
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.map.rememberPhotoMarkers
import com.routee.android.core.map.rememberRouteeMapState
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.lottie.RouteeLottie
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.pause.WorkoutPauseContent
import com.routee.android.feature.workout.component.recording.PhotoTimelineDialog
import com.routee.android.feature.workout.component.recording.WorkoutRecordingContent
import com.routee.android.feature.workout.model.MY_LOCATION_ZOOM
import com.routee.android.feature.workout.recording.model.OpenedPhoto
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import com.routee.android.feature.workout.recording.model.WorkoutSaveFailure
import com.routee.android.feature.workout.recording.model.WorkoutSummary
import com.routee.android.feature.workout.tracking.WorkoutTrackingService
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

private const val COUNTDOWN_FADE_OUT_MILLIS = 300
private const val FINISH_ANIMATION_SPEED = 1.5f
private const val MAP_CAPTURE_TIMEOUT_MILLIS = 10_000L
private val MAP_CAPTURE_PADDING = 48.dp

@Composable
internal fun WorkoutRecordingRoute(
    onStartFail: () -> Unit,
    onNavigateToTimeline: (WorkoutSummary) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: WorkoutRecordingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val messageState = LocalRouteeMessageState.current
    val endGuideText = stringResource(R.string.workout_pause_end_guide_toast)
    val startErrorText = stringResource(R.string.workout_recording_start_error_toast)
    val restoreErrorText = stringResource(R.string.workout_recording_restore_error_toast)
    val photoNoLocationText = stringResource(R.string.workout_recording_photo_no_location_toast)
    val photoFailedText = stringResource(R.string.workout_recording_photo_failed_toast)
    val photosMissingText = stringResource(R.string.workout_recording_photos_missing_toast)
    val photoDeletedText = stringResource(R.string.workout_recording_photo_deleted_snackbar)
    val photoDeletedUndoText = stringResource(R.string.workout_recording_photo_deleted_undo)
    val mapState = rememberRouteeMapState()
    val scope = rememberCoroutineScope()
    // 카메라 앱이 떠 있는 동안 앱이 종료돼도 결과를 받게 저장
    var photoFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        photoFilePath?.let { viewModel.onPhotoTaken(it, isSaved) }
        photoFilePath = null
    }

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            WorkoutRecordingSideEffect.StartTrackingService -> WorkoutTrackingService.start(context)

            WorkoutRecordingSideEffect.StartFailed -> {
                messageState.showToast(text = startErrorText)
                onStartFail()
            }

            WorkoutRecordingSideEffect.ShowRestoreFailed -> messageState.showToast(text = restoreErrorText)

            is WorkoutRecordingSideEffect.NavigateToTimeline -> onNavigateToTimeline(sideEffect.summary)

            WorkoutRecordingSideEffect.NavigateToHome -> onNavigateToHome()

            is WorkoutRecordingSideEffect.MoveToLastLocation -> scope.launch {
                mapState.moveTo(sideEffect.point, zoom = MY_LOCATION_ZOOM)
                mapState.followMyLocation()
            }

            // TODO P1 기획: 기본 카메라는 카메라 권한이 필요 없어 권한 확인 없이 실행
            is WorkoutRecordingSideEffect.TakePhoto -> try {
                photoFilePath = sideEffect.filePath
                takePhoto.launch(WorkoutPhotoFileProvider.uriFor(context, File(sideEffect.filePath)))
            } catch (e: ActivityNotFoundException) {
                Timber.w(e, "기본 카메라 없음")
                photoFilePath = null
                viewModel.onCameraUnavailable(sideEffect.filePath)
            }

            WorkoutRecordingSideEffect.ShowPhotoNoLocation -> messageState.showToast(text = photoNoLocationText)

            WorkoutRecordingSideEffect.ShowPhotoFailed -> messageState.showToast(text = photoFailedText)

            WorkoutRecordingSideEffect.ShowPhotosMissing -> messageState.showToast(text = photosMissingText)

            // 화면이 다시 만들어져 스낵바가 취소돼도 삭제 확정
            is WorkoutRecordingSideEffect.ShowPhotoDeleted -> scope.launch {
                var isUndone = false
                try {
                    val result = messageState.showSnackbar(text = photoDeletedText, actionLabel = photoDeletedUndoText)
                    isUndone = result == RouteeMessageResult.ACTION_PERFORMED
                } finally {
                    viewModel.onPhotoDeleteSnackbarClose(photoId = sideEffect.photoId, isUndone = isUndone)
                }
            }
        }
    }

    RecordingBackHandler(mode = uiState.mode)
    // 삭제 스낵바가 떠 있으면 뒤로가기 = [취소]
    BackHandler(enabled = uiState.isPhotoDeletePending) { messageState.performAction() }

    uiState.captureRoute?.let { route ->
        LaunchedEffect(route) { viewModel.onMapCaptured(mapState.captureJpeg(route)) }
    }

    WorkoutRecordingScreen(
        uiState = uiState,
        mapState = mapState,
        myLocation = viewModel.myLocation,
        onStopClick = viewModel::onStopClick,
        onCameraClick = viewModel::onCameraClick,
        onPhotoMarkerClick = viewModel::onPhotoMarkerClick,
        onCountdownFinish = viewModel::onCountdownFinish,
        onRestartClick = viewModel::onRestartClick,
        onEndClick = { messageState.showToast(text = endGuideText, duration = RouteeMessageDuration.BRIEF) },
        onEndLongPress = viewModel::onEndLongPress,
        onFinishAnimationEnd = viewModel::onFinishAnimationEnd,
    )

    uiState.openedPhoto?.let { photo ->
        OpenedPhotoDialog(
            photo = photo,
            onDeleteClick = viewModel::onPhotoDeleteClick,
            onClose = viewModel::onPhotoDialogClose,
        )
    }

    if (uiState.isCameraUnavailable) {
        RouteeOneButtonDialog(
            title = stringResource(R.string.workout_recording_camera_unavailable_title),
            description = stringResource(R.string.workout_recording_camera_unavailable_description),
            buttonText = stringResource(R.string.workout_recording_camera_unavailable_confirm),
            onButtonClick = viewModel::onCameraUnavailableDialogConfirm,
            onDismissRequest = viewModel::onCameraUnavailableDialogConfirm,
        )
    }

    uiState.saveFailure?.let { failure ->
        RouteeOneButtonDialog(
            title = stringResource(R.string.workout_recording_save_failure_title),
            description = stringResource(
                when (failure) {
                    WorkoutSaveFailure.NETWORK -> R.string.workout_recording_save_failure_network_description

                    WorkoutSaveFailure.INVALID_ROUTE ->
                        R.string.workout_recording_save_failure_invalid_route_description
                },
            ),
            buttonText = stringResource(R.string.workout_recording_save_failure_confirm),
            onButtonClick = viewModel::onSaveFailureDialogConfirm,
            onDismissRequest = viewModel::onSaveFailureDialogConfirm,
        )
    }
}

@Composable
private fun OpenedPhotoDialog(
    photo: OpenedPhoto,
    onDeleteClick: () -> Unit,
    onClose: (title: String) -> Unit,
) {
    key(photo.id) {
        val titleState = rememberTextFieldState(photo.title)
        PhotoTimelineDialog(
            imageUrl = photo.imageUrl,
            locationState = titleState,
            onDeleteClick = onDeleteClick,
            onDismissRequest = { onClose(titleState.text.toString()) },
        )
    }
}

// 경로 전체를 담은 지도 캡처 JPEG, 실패하면 null
private suspend fun RouteeMapState.captureJpeg(route: List<MapPoint>): ByteArray? = try {
    withTimeoutOrNull(MAP_CAPTURE_TIMEOUT_MILLIS) {
        val bitmap = captureRoute(route, padding = MAP_CAPTURE_PADDING)
        try {
            withContext(Dispatchers.Default) {
                ByteArrayOutputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, output))
                    output.toByteArray()
                }
            }
        } finally {
            bitmap.recycle()
        }
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Timber.e(e, "지도 캡처 실패")
    null
}

@Composable
private fun WorkoutRecordingScreen(
    uiState: WorkoutRecordingUiState,
    mapState: RouteeMapState,
    myLocation: Flow<Location>?,
    onStopClick: () -> Unit,
    onCameraClick: () -> Unit,
    onPhotoMarkerClick: (String) -> Unit,
    onCountdownFinish: () -> Unit,
    onRestartClick: () -> Unit,
    onEndClick: () -> Unit,
    onEndLongPress: () -> Unit,
    onFinishAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(mapState) { mapState.followMyLocation() }
    val photoMarkers = rememberPhotoMarkers(uiState.photos)

    RouteeNaverMap(
        modifier = modifier.fillMaxSize(),
        state = mapState,
        myLocation = if (uiState.isMapOverlayVisible) myLocation else null,
        route = if (uiState.isMapOverlayVisible) uiState.route else persistentListOf(),
        markers = if (uiState.isMapOverlayVisible) photoMarkers else emptyList(),
        onMarkerClick = onPhotoMarkerClick,
    ) {
        AnimatedContent(
            targetState = uiState.mode,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { workoutModeTransition() },
        ) { mode ->
            when (mode) {
                WorkoutMode.COUNTDOWN -> RouteeLottie(
                    resId = DesignSystemR.raw.lottie_workout_countdown,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RouteeTheme.colors.black)
                        .pointerInput(Unit) {},
                    contentScale = ContentScale.Crop,
                    onFinish = onCountdownFinish,
                )

                WorkoutMode.RECORDING -> WorkoutRecordingContent(
                    distanceMeters = uiState.distanceMeters,
                    durationSec = uiState.durationSec,
                    maxElevationMeters = uiState.maxElevationMeters,
                    photoCount = uiState.photoCount,
                    isCameraEnabled = uiState.isCameraEnabled,
                    onMyLocationClick = { scope.launch { mapState.followMyLocation() } },
                    onStopClick = onStopClick,
                    onCameraClick = onCameraClick,
                )

                WorkoutMode.PAUSED -> WorkoutPauseContent(
                    distanceMeters = uiState.distanceMeters,
                    durationSec = uiState.durationSec,
                    maxElevationMeters = uiState.maxElevationMeters,
                    onRestartClick = onRestartClick,
                    onEndClick = onEndClick,
                    onEndLongPress = onEndLongPress,
                )

                WorkoutMode.FINISHING -> RouteeLottie(
                    resId = DesignSystemR.raw.lottie_workout_finish,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {},
                    speed = FINISH_ANIMATION_SPEED,
                    contentScale = ContentScale.Crop,
                    onFinish = onFinishAnimationEnd,
                )
            }
        }
        // 네이버 로고: 오른쪽 아래
        Spacer(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .mapLogoAbove(
                    spacing = RouteeTheme.spacing.Spacing10,
                    alignment = Alignment.End,
                    inset = RouteeTheme.spacing.Spacing14,
                ),
        )
    }
}

private fun AnimatedContentTransitionScope<WorkoutMode>.workoutModeTransition(): ContentTransform =
    if (initialState == WorkoutMode.COUNTDOWN) {
        (EnterTransition.None togetherWith fadeOut(tween(COUNTDOWN_FADE_OUT_MILLIS))).apply {
            targetContentZIndex = -1f
        }
    } else {
        EnterTransition.None togetherWith ExitTransition.None
    }

@Composable
private fun RecordingBackHandler(mode: WorkoutMode) {
    val messageState = LocalRouteeMessageState.current
    val activity = LocalActivity.current
    val exitText = stringResource(R.string.workout_recording_exit_toast)
    var lastBackPressedAt by remember(mode) { mutableLongStateOf(0L) }

    BackHandler(enabled = mode != WorkoutMode.COUNTDOWN) {
        if (mode == WorkoutMode.FINISHING) return@BackHandler
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackPressedAt < RouteeMessageDuration.MEDIUM.millis) {
            activity?.finish()
        } else {
            lastBackPressedAt = now
            messageState.showToast(text = exitText, duration = RouteeMessageDuration.MEDIUM)
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun WorkoutRecordingScreenPreview() {
    RouteePreviewTheme {
        WorkoutRecordingScreen(
            mapState = rememberRouteeMapState(),
            uiState = WorkoutRecordingUiState(
                distanceMeters = 15_530,
                durationSec = 12_000,
                maxElevationMeters = 2132,
                mode = WorkoutMode.RECORDING,
                photos = List(20) { MapPhoto(id = "$it", imageUrl = "", point = MapPoint(0.0, 0.0)) }.toImmutableList(),
            ),
            myLocation = null,
            onStopClick = {},
            onCameraClick = {},
            onPhotoMarkerClick = {},
            onCountdownFinish = {},
            onRestartClick = {},
            onEndClick = {},
            onEndLongPress = {},
            onFinishAnimationEnd = {},
        )
    }
}
