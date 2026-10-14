package com.routee.android.feature.workout.component.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

private val ControlIconSize = 28.dp

/**
 * 운동 정지 버튼 (cta_md_workout_stop)
 *
 * @param isStopped 운동이 멈춘 상태. true면 다시 시작, false면 정지를 보여준다
 */
@Composable
internal fun WorkoutStopButton(
    isStopped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = RouteeTheme.colors.bgPrimary
    val iconRes = if (isStopped) DesignSystemR.drawable.ic_restart else DesignSystemR.drawable.ic_stop
    val textRes = if (isStopped) R.string.workout_main_restart else R.string.workout_main_stop

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Full))
            .background(RouteeTheme.colors.white)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = RouteeTheme.spacing.Spacing24, vertical = RouteeTheme.spacing.Spacing16),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(ControlIconSize),
            tint = contentColor,
        )
        Text(
            text = stringResource(textRes),
            style = RouteeTheme.typography.labelSb16,
            color = contentColor,
        )
    }
}

@PreviewFontSizes
@Composable
private fun WorkoutStopButtonPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutStopButton(isStopped = false, onClick = {})
            WorkoutStopButton(isStopped = true, onClick = {})
        }
    }
}
