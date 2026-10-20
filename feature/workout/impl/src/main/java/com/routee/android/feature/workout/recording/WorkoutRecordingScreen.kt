package com.routee.android.feature.workout.recording

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.mapLogoAbove
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.lottie.RouteeLottie
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.pause.WorkoutPauseContent
import com.routee.android.feature.workout.component.recording.WorkoutRecordingContent
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState

private const val COUNTDOWN_FADE_OUT_MILLIS = 300
private const val FINISH_ANIMATION_SPEED = 1.5f

@Composable
internal fun WorkoutRecordingRoute(
    onFinish: () -> Unit,
    viewModel: WorkoutRecordingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageState = LocalRouteeMessageState.current
    val endGuideText = stringResource(R.string.workout_pause_end_guide_toast)

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            WorkoutRecordingSideEffect.NavigateToTimeline -> onFinish()
        }
    }

    RecordingBackHandler(mode = uiState.mode)

    WorkoutRecordingScreen(
        uiState = uiState,
        // TODO 지도를 내 위치로 이동
        onMyLocationClick = {},
        onStopClick = viewModel::onStopClick,
        // TODO 카메라 권한 확인 후 촬영
        onCameraClick = {},
        onCountdownFinish = viewModel::onCountdownFinish,
        onRestartClick = viewModel::onRestartClick,
        onEndClick = { messageState.showToast(text = endGuideText, duration = RouteeMessageDuration.BRIEF) },
        onEndLongPress = viewModel::onEndLongPress,
        onFinishAnimationEnd = viewModel::onFinishAnimationEnd,
    )
}

@Composable
private fun WorkoutRecordingScreen(
    uiState: WorkoutRecordingUiState,
    onMyLocationClick: () -> Unit,
    onStopClick: () -> Unit,
    onCameraClick: () -> Unit,
    onCountdownFinish: () -> Unit,
    onRestartClick: () -> Unit,
    onEndClick: () -> Unit,
    onEndLongPress: () -> Unit,
    onFinishAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO 내 위치, 경로선, 사진 마커 연결
    RouteeNaverMap(modifier = modifier.fillMaxSize()) {
        if (uiState.mode != WorkoutMode.FINISHING) {
            WorkoutRecordingContent(
                distanceMeters = uiState.distanceMeters,
                durationSec = uiState.durationSec,
                maxElevationMeters = uiState.maxElevationMeters,
                photoCount = uiState.photoCount,
                isCameraEnabled = uiState.isCameraEnabled,
                onMyLocationClick = onMyLocationClick,
                onStopClick = onStopClick,
                onCameraClick = onCameraClick,
            )
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
        AnimatedVisibility(
            visible = uiState.mode == WorkoutMode.COUNTDOWN,
            enter = EnterTransition.None,
            exit = fadeOut(tween(COUNTDOWN_FADE_OUT_MILLIS)),
        ) {
            RouteeLottie(
                resId = DesignSystemR.raw.lottie_workout_countdown,
                modifier = Modifier
                    .fillMaxSize()
                    .background(RouteeTheme.colors.black)
                    .pointerInput(Unit) {},
                contentScale = ContentScale.Crop,
                onFinish = onCountdownFinish,
            )
        }
        if (uiState.mode == WorkoutMode.PAUSED) {
            WorkoutPauseContent(
                distanceMeters = uiState.distanceMeters,
                durationSec = uiState.durationSec,
                maxElevationMeters = uiState.maxElevationMeters,
                onRestartClick = onRestartClick,
                onEndClick = onEndClick,
                onEndLongPress = onEndLongPress,
            )
        }
        if (uiState.mode == WorkoutMode.FINISHING) {
            RouteeLottie(
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
            uiState = WorkoutRecordingUiState(
                distanceMeters = 15_530,
                durationSec = 12_000,
                maxElevationMeters = 2132,
                mode = WorkoutMode.RECORDING,
                photoCount = 20,
            ),
            onMyLocationClick = {},
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
