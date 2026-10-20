package com.routee.android.feature.workout.recording

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.mapLogoAbove
import com.routee.android.core.ui.lottie.RouteeLottie
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.main.MyLocationButton
import com.routee.android.feature.workout.component.main.WorkoutStopButton
import com.routee.android.feature.workout.component.main.WorkoutTopGradient
import com.routee.android.feature.workout.component.recording.CameraButton
import com.routee.android.feature.workout.recording.model.MAX_PHOTO_COUNT
import com.routee.android.feature.workout.recording.model.WorkoutRecordingUiState
import java.util.Locale

private val MetricTopSpacing = 24.dp
private val ControlBottomSpacing = 28.dp
private const val COUNTDOWN_FADE_OUT_MILLIS = 300

@Composable
internal fun WorkoutRecordingRoute(
    viewModel: WorkoutRecordingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecordingExitBackHandler(enabled = !uiState.isCountingDown)

    WorkoutRecordingScreen(
        uiState = uiState,
        // TODO 지도를 내 위치로 이동
        onMyLocationClick = {},
        // TODO 정지 화면
        onStopClick = {},
        // TODO 카메라 권한 확인 후 촬영
        onCameraClick = {},
        onCountdownFinish = viewModel::onCountdownFinish,
    )
}

@Composable
private fun WorkoutRecordingScreen(
    uiState: WorkoutRecordingUiState,
    onMyLocationClick: () -> Unit,
    onStopClick: () -> Unit,
    onCameraClick: () -> Unit,
    onCountdownFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO 내 위치, 경로선, 사진 마커 연결
    RouteeNaverMap(modifier = modifier.fillMaxSize()) {
        WorkoutTopGradient(modifier = Modifier.align(Alignment.TopCenter))
        RouteeWorkoutMetricCard(
            distance = String.format(Locale.US, "%.2f", uiState.distanceMeters / 1000.0),
            duration = formatDuration(uiState.durationSec),
            maxElevation = uiState.maxElevationMeters.toString(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(
                    start = RouteeTheme.spacing.Spacing16,
                    end = RouteeTheme.spacing.Spacing16,
                    top = MetricTopSpacing,
                )
                .fillMaxWidth(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = RouteeTheme.spacing.Spacing16,
                    end = RouteeTheme.spacing.Spacing16,
                    bottom = ControlBottomSpacing,
                ),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
            horizontalAlignment = Alignment.End,
        ) {
            MyLocationButton(onClick = onMyLocationClick)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .routeeMessageAnchor(),
                horizontalArrangement = Arrangement.spacedBy(
                    RouteeTheme.spacing.Spacing12,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WorkoutStopButton(isStopped = false, onClick = onStopClick)
                CameraButton(
                    count = "${uiState.photoCount}/$MAX_PHOTO_COUNT",
                    onClick = onCameraClick,
                    enabled = uiState.isCameraEnabled,
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
        AnimatedVisibility(
            visible = uiState.isCountingDown,
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
    }
}

@Composable
private fun RecordingExitBackHandler(enabled: Boolean) {
    val messageState = LocalRouteeMessageState.current
    val activity = LocalActivity.current
    val exitText = stringResource(R.string.workout_recording_exit_toast)
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = enabled) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackPressedAt < RouteeMessageDuration.MEDIUM.millis) {
            activity?.finish()
        } else {
            lastBackPressedAt = now
            messageState.showToast(text = exitText, duration = RouteeMessageDuration.MEDIUM)
        }
    }
}

private fun formatDuration(durationSec: Int): String {
    val hours = durationSec / 3600
    val minutes = durationSec % 3600 / 60
    return String.format(Locale.US, "%02d:%02d", hours, minutes)
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
                isCountingDown = false,
                photoCount = 20,
            ),
            onMyLocationClick = {},
            onStopClick = {},
            onCameraClick = {},
            onCountdownFinish = {},
        )
    }
}
