package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.extension.noRippleSelectable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapEditTab

// recap_tab_android
private val SelectedIndicatorThickness = 2.dp

/**
 * 기록 편집 하단 편집 탭의 항목 (recap_tab)
 */
@Composable
internal fun RecapEditTabItem(
    tab: RecapEditTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RouteeTheme.colors
    val contentColor = if (isSelected) colors.white else colors.grey200
    val indicatorColor = colors.white30

    Column(
        modifier = modifier
            .background(if (isSelected) colors.white10 else Color.Transparent)
            .drawBehind {
                if (isSelected) {
                    val strokeWidth = SelectedIndicatorThickness.toPx()
                    drawLine(
                        color = indicatorColor,
                        start = Offset(0f, strokeWidth / 2),
                        end = Offset(size.width, strokeWidth / 2),
                        strokeWidth = strokeWidth,
                    )
                }
            }
            .noRippleSelectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .padding(vertical = RouteeTheme.spacing.Spacing14),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing2),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(if (isSelected) tab.selectedIconRes else tab.unselectedIconRes),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Text(
            text = stringResource(tab.labelRes),
            style = if (isSelected) RouteeTheme.typography.labelSb12 else RouteeTheme.typography.labelM12,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private class RecapEditTabItemPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@PreviewFontSizes
@Composable
private fun RecapEditTabItemPreview(
    @PreviewParameter(RecapEditTabItemPreviewParameterProvider::class) isSelected: Boolean,
) {
    RouteePreviewTheme {
        RecapEditTabItem(
            tab = RecapEditTab.BACKGROUND,
            isSelected = isSelected,
            onClick = {},
        )
    }
}
