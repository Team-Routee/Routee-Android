package com.routee.android.feature.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.R

private const val MOUNTAIN_MAP_ROW_COUNT = 3
private const val MOUNTAIN_MAP_COLUMN_COUNT = 11
internal const val MOUNTAIN_MAP_CELL_COUNT = MOUNTAIN_MAP_ROW_COUNT * MOUNTAIN_MAP_COLUMN_COUNT

internal enum class MountainActivityLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH,
}

@Composable
private fun MountainMapCell(
    activityLevel: MountainActivityLevel,
) {
    val color = when (activityLevel) {
        MountainActivityLevel.NONE -> RouteeTheme.colors.white10
        MountainActivityLevel.LOW -> RouteeTheme.colors.mint900
        MountainActivityLevel.MEDIUM -> RouteeTheme.colors.mint700
        MountainActivityLevel.HIGH -> RouteeTheme.colors.mint500
        MountainActivityLevel.VERY_HIGH -> RouteeTheme.colors.mint200
    }

    Icon(
        imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_mountain_map_cell),
        contentDescription = null,
        tint = color,
    )
}

@Composable
internal fun MountainMap(
    activityLevels: List<MountainActivityLevel>,
    modifier: Modifier = Modifier,
) {
    require(activityLevels.size == MOUNTAIN_MAP_CELL_COUNT) {
        "MountainMap requires $MOUNTAIN_MAP_CELL_COUNT activity levels."
    }

    Column(
        modifier = modifier
            .background(
                color = RouteeTheme.colors.grey900,
                shape = RoundedCornerShape(RouteeTheme.radius.Radius12),
            )
            .padding(top = RouteeTheme.spacing.Spacing20, bottom = RouteeTheme.spacing.Spacing10),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6),
        ) {
            repeat(MOUNTAIN_MAP_ROW_COUNT) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(MOUNTAIN_MAP_COLUMN_COUNT) { column ->
                        MountainMapCell(
                            activityLevel = activityLevels[row * MOUNTAIN_MAP_COLUMN_COUNT + column],
                        )
                    }
                }
            }
        }

        MountainMapLegend(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = RouteeTheme.spacing.Spacing10,
                    start = RouteeTheme.spacing.Spacing20,
                    end = RouteeTheme.spacing.Spacing20,
                ),
        )
    }
}

@Composable
private fun MountainMapLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.archive_mountain_map_less),
            color = RouteeTheme.colors.white30,
            style = RouteeTheme.typography.labelR12,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4)) {
            IntensityStep(color = RouteeTheme.colors.white10)
            IntensityStep(color = RouteeTheme.colors.mint900)
            IntensityStep(color = RouteeTheme.colors.mint700)
            IntensityStep(color = RouteeTheme.colors.mint500)
            IntensityStep(color = RouteeTheme.colors.mint200)
        }
        Text(
            text = stringResource(R.string.archive_mountain_map_more),
            color = RouteeTheme.colors.white30,
            style = RouteeTheme.typography.labelR12,
        )
    }
}

@Composable
private fun IntensityStep(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 4.dp, height = 8.dp)
            .background(color = color, shape = RoundedCornerShape(2.dp)),
    )
}

@PreviewFontSizes
@Composable
private fun MountainMapCellPreview() {
    RouteePreviewTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing10)) {
            MountainActivityLevel.entries.reversed().forEach { level ->
                MountainMapCell(activityLevel = level)
            }
        }
    }
}

@PreviewFontSizes
@Composable
private fun MountainMapPreview() {
    RouteePreviewTheme {
        MountainMap(
            activityLevels = List(MOUNTAIN_MAP_CELL_COUNT) { index ->
                MountainActivityLevel.entries[index % MountainActivityLevel.entries.size]
            },
            modifier = Modifier.width(328.dp),
        )
    }
}
