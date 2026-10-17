package com.routee.android.feature.workout.component.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.routee.android.core.designsystem.component.RouteeAutoSizeText
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

/**
 * 운동 수치 대형 텍스트 (info_text_main)
 *
 * 거리, 시간, 최고 고도를 라벨 아래 큰 글자로 세로로 쌓는다. 라벨과 값이 화면보다 길면 한 줄을 유지하며 글자를 줄인다
 *
 * @param distance 거리. 단위까지 포함한 문자열 (예: "15.5km")
 * @param duration 운동 시간 (예: "03h 03m")
 * @param maxElevation 최고 고도 (예: "2132m")
 */
@Composable
internal fun WorkoutMetricLargeText(
    distance: String,
    duration: String,
    maxElevation: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MetricItem(label = stringResource(R.string.workout_main_metric_distance), value = distance)
        MetricItem(label = stringResource(R.string.workout_main_metric_duration), value = duration)
        MetricItem(label = stringResource(R.string.workout_main_metric_max_elevation), value = maxElevation)
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RouteeAutoSizeText(
            text = label,
            style = RouteeTheme.typography.titleSb18,
            color = RouteeTheme.colors.black60,
            textAlign = TextAlign.Center,
        )
        RouteeAutoSizeText(
            text = value,
            style = RouteeTheme.typography.displayB42,
            color = RouteeTheme.colors.black,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewFontSizes
@Composable
private fun WorkoutMetricLargeTextPreview() {
    RouteeTheme {
        WorkoutMetricLargeText(
            distance = "15.5km",
            duration = "03h 03m",
            maxElevation = "2132m",
            modifier = Modifier
                .background(RouteeTheme.colors.bgHighlight)
                .padding(RouteeTheme.spacing.Spacing24),
        )
    }
}
