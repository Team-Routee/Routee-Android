package com.routee.android.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteeTheme

// global_nav_bar_android
private val NavItemSpacing = 5.dp

/**
 * 하단 플로팅 탭 바
 *
 * 항목은 [RouteeNavBarItem]으로 구성. 폭은 호출하는 쪽에서 결정
 */
@Composable
fun RouteeNavBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Full))
            .background(RouteeTheme.colors.grey900)
            .padding(RouteeTheme.spacing.Spacing6)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(NavItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/**
 * [RouteeNavBar]의 탭 항목
 */
@Composable
fun RowScope.RouteeNavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) RouteeTheme.colors.white else RouteeTheme.colors.grey400

    Column(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius24))
            .background(if (selected) RouteeTheme.colors.white10 else Color.Transparent)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(vertical = RouteeTheme.spacing.Spacing4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = label,
            style = RouteeTheme.typography.labelM12,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "RouteeNavBar", widthDp = 360)
@Preview(name = "RouteeNavBar - 글자 200%", widthDp = 360, fontScale = 2f)
@Composable
private fun RouteeNavBarPreview() {
    RouteeTheme {
        RouteeNavBar(modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 8.dp)) {
            RouteeNavBarItem(
                selected = true,
                onClick = {},
                iconRes = R.drawable.ic_exercise_nav_sm_white,
                label = "운동",
            )
            RouteeNavBarItem(
                selected = false,
                onClick = {},
                iconRes = R.drawable.ic_recap_nav_sm_white,
                label = "기록 편집",
            )
            RouteeNavBarItem(
                selected = false,
                onClick = {},
                iconRes = R.drawable.ic_archive_nav_sm_white,
                label = "아카이브",
            )
            RouteeNavBarItem(
                selected = false,
                onClick = {},
                iconRes = R.drawable.ic_setting_nav_sm_white,
                label = "설정",
            )
        }
    }
}
