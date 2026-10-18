package com.routee.android.feature.archive.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeRouteMarker
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.R

private const val COLLAPSED_POINT_COUNT = 3
private val RouteLeadingWidth = 28.dp
private val RouteCardMinHeight = 44.dp

@Composable
internal fun RoutePointItem(
    points: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val collapsible = points.size > COLLAPSED_POINT_COUNT
    val visiblePointCount = if (collapsible && !expanded) COLLAPSED_POINT_COUNT else points.size

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (collapsible) {
                        Modifier.noRippleClickable(role = Role.Button) { onExpandedChange(!expanded) }
                    } else {
                        Modifier
                    },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.archive_route_point_item_title),
                color = RouteeTheme.colors.white,
                style = RouteeTheme.typography.titleSb18,
                modifier = Modifier.padding(start = 2.5.dp),
            )
            if (collapsible) {
                Image(
                    imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_chevron_down_sm_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f),
                )
            }
        }

        Column {
            repeat(visiblePointCount) { index ->
                RoutePointRow(
                    number = index + 1,
                    name = points[index],
                    hasNextPoint = index < visiblePointCount - 1,
                )
                if (index < visiblePointCount - 1) RouteConnector()
            }
        }
    }
}

@Composable
private fun RoutePointRow(
    number: Int,
    name: String,
    hasNextPoint: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
    ) {
        Column(
            modifier = Modifier
                .width(RouteLeadingWidth)
                .fillMaxHeight()
                .padding(top = RouteeTheme.spacing.Spacing10),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        ) {
            RouteeRouteMarker(number = number)
            if (hasNextPoint) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(1.dp)
                        .background(RouteeTheme.colors.grey400, CircleShape),
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = RouteCardMinHeight)
                .background(
                    color = RouteeTheme.colors.grey800,
                    shape = RoundedCornerShape(RouteeTheme.radius.Radius12),
                )
                .padding(RouteeTheme.spacing.Spacing12),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = name,
                color = RouteeTheme.colors.white,
                style = RouteeTheme.typography.labelM14,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RouteConnector() {
    Box(
        modifier = Modifier.size(width = RouteLeadingWidth, height = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 12.dp)
                .background(RouteeTheme.colors.grey400, CircleShape),
        )
    }
}

@PreviewFontSizes
@Composable
private fun RoutePointItemPreview() {
    RouteeTheme {
        Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24)) {
            RoutePointItem(
                points = listOf("창의문", "청운대", "말바위"),
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.width(328.dp),
            )
            RoutePointItem(
                points = listOf("창의문", "청운대", "말바위", "백숙 먹으러 가는 중"),
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.width(328.dp),
            )
            RoutePointItem(
                points = listOf("창의문", "청운대", "말바위", "백숙 먹으러 가는 중"),
                expanded = true,
                onExpandedChange = {},
                modifier = Modifier.width(328.dp),
            )
        }
    }
}
