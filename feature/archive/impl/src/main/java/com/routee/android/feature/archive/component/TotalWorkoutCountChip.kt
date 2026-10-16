package com.routee.android.feature.archive.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteeTheme

@Composable
internal fun TotalWorkoutCountChip(
    workoutCount: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius14)

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = RouteeTheme.colors.mint700,
                shape = shape,
            )
            .background(
                color = RouteeTheme.colors.surfaceTinted,
                shape = shape,
            )
            .padding(
                start = RouteeTheme.spacing.Spacing8,
                top = RouteeTheme.spacing.Spacing2,
                end = RouteeTheme.spacing.Spacing12,
                bottom = RouteeTheme.spacing.Spacing2,
            ),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_mountain_sm_gradient),
            contentDescription = null,
        )
        Text(
            text = workoutCount.toString(),
            color = RouteeTheme.colors.white,
            style = RouteeTheme.typography.labelR12,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun TotalWorkoutCountChipPreview() {
    RouteeTheme {
        TotalWorkoutCountChip(workoutCount = 25)
    }
}
