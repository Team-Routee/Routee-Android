package com.routee.android.feature.workout.component.recording

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.component.main.MyLocationButton
import com.routee.android.feature.workout.component.main.WorkoutStopButton
import com.routee.android.feature.workout.component.main.WorkoutTopGradient
import com.routee.android.feature.workout.recording.model.MAX_PHOTO_COUNT
import java.util.Locale

private val MetricTopSpacing = 24.dp
private val ControlBottomSpacing = 28.dp

/**
 * 운동 중 화면
 *
 * @param distanceMeters     거리(m)
 * @param durationSec        운동 시간(초)
 * @param maxElevationMeters 최고 고도(m)
 * @param photoCount         촬영한 사진 수
 * @param isCameraEnabled    false 이면 카메라 버튼 비활성
 * @param onMyLocationClick  내 위치 버튼 탭 시 호출
 * @param onStopClick        정지 탭 시 호출
 * @param onCameraClick      카메라 탭 시 호출
 */
@Composable
internal fun WorkoutRecordingContent(
    distanceMeters: Int,
    durationSec: Int,
    maxElevationMeters: Int,
    photoCount: Int,
    isCameraEnabled: Boolean,
    onMyLocationClick: () -> Unit,
    onStopClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        WorkoutTopGradient(modifier = Modifier.align(Alignment.TopCenter))
        RouteeWorkoutMetricCard(
            distance = formatDistanceKm(distanceMeters),
            duration = formatDuration(durationSec),
            maxElevation = maxElevationMeters.toString(),
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
                    count = "$photoCount/$MAX_PHOTO_COUNT",
                    onClick = onCameraClick,
                    enabled = isCameraEnabled,
                )
            }
        }
    }
}

private fun formatDistanceKm(distanceMeters: Int): String = String.format(Locale.US, "%.2f", distanceMeters / 1000.0)

private fun formatDuration(durationSec: Int): String =
    String.format(Locale.US, "%02d:%02d", durationSec / 3600, durationSec % 3600 / 60)

@PreviewDeviceSizes
@Composable
private fun WorkoutRecordingContentPreview() {
    RouteePreviewTheme {
        WorkoutRecordingContent(
            distanceMeters = 15_530,
            durationSec = 12_000,
            maxElevationMeters = 2132,
            photoCount = 20,
            isCameraEnabled = false,
            onMyLocationClick = {},
            onStopClick = {},
            onCameraClick = {},
        )
    }
}
