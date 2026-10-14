package com.routee.android.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

// workout_metric_android
private val MetricItemSpacing = 4.dp

/**
 * 운동 수치 카드
 *
 * 거리, 시간, 최고 고도를 세 칸에 같은 폭으로 나눠 보여준다. 라벨과 값이 칸보다 길면 한 줄을 유지하며 글자를 줄인다. 카드 폭은 [modifier]로 정한다 (예: `Modifier.fillMaxWidth()`)
 *
 * @param distance 거리(km). 화면에 보일 문자열 (예: "15.53")
 * @param duration 운동 시간 (예: "03:20")
 * @param maxElevation 최고 고도(m) (예: "2132")
 */
@Composable
fun RouteeWorkoutMetricCard(
    distance: String,
    duration: String,
    maxElevation: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius12))
            .background(RouteeTheme.colors.black60)
            .padding(vertical = RouteeTheme.spacing.Spacing16),
        horizontalArrangement = Arrangement.spacedBy(MetricItemSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetricItem(label = stringResource(R.string.designsystem_workout_metric_distance), value = distance)
        MetricItem(label = stringResource(R.string.designsystem_workout_metric_duration), value = duration)
        MetricItem(label = stringResource(R.string.designsystem_workout_metric_max_elevation), value = maxElevation)
    }
}

@Composable
private fun RowScope.MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RouteeAutoSizeText(
            text = label,
            style = RouteeTheme.typography.labelSb12,
            color = RouteeTheme.colors.mint300,
            textAlign = TextAlign.Center,
        )
        RouteeAutoSizeText(
            text = value,
            style = RouteeTheme.typography.display30,
            color = RouteeTheme.colors.mint300,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewFontSizes
@Composable
private fun RouteeWorkoutMetricCardPreview() {
    RouteeTheme {
        RouteeWorkoutMetricCard(
            distance = "15.53",
            duration = "03:20",
            maxElevation = "2132",
            modifier = Modifier.padding(16.dp),
        )
    }
}
