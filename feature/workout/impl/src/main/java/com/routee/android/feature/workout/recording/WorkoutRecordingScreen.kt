package com.routee.android.feature.workout.recording

import android.graphics.Bitmap
import android.location.Location
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.dialog.RouteeOneButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeMapState
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.mapLogoAbove
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.map.rememberRouteeMapState
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.lottie.RouteeLottie
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.pause.WorkoutPauseContent
import com.routee.android.feature.workout.component.recording.WorkoutRecordingContent
import com.routee.android.feature.workout.recording.model.MY_LOCATION_ZOOM
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import com.routee.android.feature.workout.recording.model.WorkoutSaveFailure
import com.routee.android.feature.workout.recording.model.WorkoutSummary
import com.routee.android.feature.workout.tracking.WorkoutTrackingService
import java.io.ByteArrayOutputStream
import kotlinx.collections.immutable.persistentListOf
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
private const val MAP_CAPTURE_JPEG_QUALITY = 80
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
    val mapState = rememberRouteeMapState()
    val scope = rememberCoroutineScope()

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
        }
    }

    RecordingBackHandler(mode = uiState.mode)

    uiState.captureRoute?.let { route ->
        LaunchedEffect(route) { viewModel.onMapCaptured(mapState.captureJpeg(route)) }
    }

    WorkoutRecordingScreen(
        uiState = uiState,
        mapState = mapState,
        myLocation = viewModel.myLocation,
        onStopClick = viewModel::onStopClick,
        // TODO 카메라 권한 확인 후 촬영
        onCameraClick = {},
        onCountdownFinish = viewModel::onCountdownFinish,
        onRestartClick = viewModel::onRestartClick,
        onEndClick = { messageState.showToast(text = endGuideText, duration = RouteeMessageDuration.BRIEF) },
        onEndLongPress = viewModel::onEndLongPress,
        onFinishAnimationEnd = viewModel::onFinishAnimationEnd,
    )

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

// 경로 전체를 담은 지도 캡처 JPEG, 실패하면 null
private suspend fun RouteeMapState.captureJpeg(route: List<MapPoint>): ByteArray? = try {
    withTimeoutOrNull(MAP_CAPTURE_TIMEOUT_MILLIS) {
        val bitmap = captureRoute(route, padding = MAP_CAPTURE_PADDING)
        try {
            withContext(Dispatchers.Default) {
                ByteArrayOutputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, MAP_CAPTURE_JPEG_QUALITY, output))
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
    onCountdownFinish: () -> Unit,
    onRestartClick: () -> Unit,
    onEndClick: () -> Unit,
    onEndLongPress: () -> Unit,
    onFinishAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(mapState) { mapState.followMyLocation() }

    // TODO 사진 마커 연결
    RouteeNaverMap(
        modifier = modifier.fillMaxSize(),
        state = mapState,
        myLocation = if (uiState.isMapOverlayVisible) myLocation else null,
        route = if (uiState.isMapOverlayVisible) uiState.route else persistentListOf(),
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
                photoCount = 20,
            ),
            myLocation = null,
            onStopClick = {},
            onCameraClick = {},
            onCountdownFinish = {},
            onRestartClick = {},
            onEndClick = {},
            onEndLongPress = {},
            onFinishAnimationEnd = {},
        )
    }
}
