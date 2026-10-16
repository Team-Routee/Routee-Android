package com.routee.android.feature.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.R

@Composable
internal fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    require(count > 0) { "CountBadge count must be positive." }

    Box(
        modifier = modifier
            .size(width = if (count > 9) 24.dp else 16.dp, height = 16.dp)
            .background(
                color = RouteeTheme.colors.mint300,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 9) stringResource(R.string.archive_count_badge_overflow) else count.toString(),
            color = RouteeTheme.colors.mint900,
            style = RouteeTheme.typography.labelSb12,
        )
    }
}

@PreviewFontSizes
@Composable
private fun CountBadgePreview() {
    RouteeTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4)) {
            (1..10).forEach { count ->
                CountBadge(count = count)
            }
        }
    }
}
