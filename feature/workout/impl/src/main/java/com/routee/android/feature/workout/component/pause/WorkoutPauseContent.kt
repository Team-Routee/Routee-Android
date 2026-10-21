package com.routee.android.feature.workout.component.pause

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.format.formatDistanceKm
import com.routee.android.core.ui.format.formatDurationHourMinute
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.component.main.WorkoutMetricLargeText
import com.routee.android.feature.workout.component.main.WorkoutStopButton

private val MetricOffsetY = (-26).dp
private val ControlBottomSpacing = 28.dp

/**
 * 운동 정지 화면
 *
 * @param distanceMeters     거리(m)
 * @param durationSec        운동 시간(초)
 * @param maxElevationMeters 최고 고도(m)
 * @param onRestartClick     다시 시작 탭 시 호출
 * @param onEndClick         종료 버튼을 1.5초 전에 떼면 호출
 * @param onEndLongPress     종료 버튼을 1.5초 누르면 호출
 */
@Composable
internal fun WorkoutPauseContent(
    distanceMeters: Int,
    durationSec: Int,
    maxElevationMeters: Int,
    onRestartClick: () -> Unit,
    onEndClick: () -> Unit,
    onEndLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightSystemBarsEffect()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RouteeTheme.colors.bgHighlight)
            .pointerInput(Unit) {},
    ) {
        WorkoutMetricLargeText(
            distance = stringResource(R.string.workout_pause_metric_distance_value, formatDistanceKm(distanceMeters)),
            duration = formatDurationHourMinute(durationSec),
            maxElevation = stringResource(
                R.string.workout_pause_metric_max_elevation_value,
                maxElevationMeters.toString(),
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = MetricOffsetY)
                .padding(horizontal = RouteeTheme.spacing.Spacing16),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = ControlBottomSpacing)
                .routeeMessageAnchor(),
            horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WorkoutStopButton(isStopped = true, onClick = onRestartClick)
            WorkoutEndButton(onClick = onEndClick, onLongPress = onEndLongPress)
        }
    }
}

// 밝은 배경이라 상태 바 아이콘 어둡게
@Composable
private fun LightSystemBarsEffect() {
    val activity = LocalActivity.current
    val view = LocalView.current
    DisposableEffect(activity, view) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        onDispose {
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun WorkoutPauseContentPreview() {
    RouteePreviewTheme {
        WorkoutPauseContent(
            distanceMeters = 15_530,
            durationSec = 11_000,
            maxElevationMeters = 2132,
            onRestartClick = {},
            onEndClick = {},
            onEndLongPress = {},
        )
    }
}
