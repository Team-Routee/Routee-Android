package com.routee.android.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import kotlin.math.roundToInt

// global_nav_bar_android
private val NavItemSpacing = 5.dp
private const val INDICATOR_DURATION_MILLIS = 100

/**
 * 하단 플로팅 탭 바, 가로 폭이 정해진 곳에서 사용
 *
 * @param selectedIndex 선택 표시를 둘 항목 순서, 바뀌면 선택 표시가 이동
 * @param content       [RouteeNavBarItem] 목록
 */
@Composable
fun RouteeNavBar(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animatedSelectedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(INDICATOR_DURATION_MILLIS),
        label = "RouteeNavBarIndicator",
    )

    Layout(
        content = {
            SelectionIndicator()
            content()
        },
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Full))
            .background(RouteeTheme.colors.grey900)
            .padding(RouteeTheme.spacing.Spacing6)
            .selectableGroup(),
    ) { measurables, constraints ->
        val indicator = measurables.first()
        val items = measurables.drop(1)

        // 항목 같은 폭으로 나눔
        val gap = NavItemSpacing.roundToPx()
        val itemWidth = (constraints.maxWidth - gap * (items.size - 1)) / items.size
        fun xOf(index: Float) = ((itemWidth + gap) * index).roundToInt()

        val itemPlaceables = items.map { it.measure(Constraints.fixedWidth(itemWidth)) }
        val barHeight = itemPlaceables.maxOf { it.height }
        val indicatorPlaceable = indicator.measure(Constraints.fixed(itemWidth, barHeight))

        layout(constraints.maxWidth, barHeight) {
            // 선택 표시는 항목 뒤, 선택 칸 위치로 이동
            indicatorPlaceable.placeRelative(x = xOf(animatedSelectedIndex), y = 0)
            itemPlaceables.forEachIndexed { index, item ->
                item.placeRelative(x = xOf(index.toFloat()), y = 0)
            }
        }
    }
}

@Composable
private fun SelectionIndicator() {
    Box(
        Modifier.background(
            color = RouteeTheme.colors.white10,
            shape = RoundedCornerShape(RouteeTheme.radius.Radius24),
        ),
    )
}

/**
 * [RouteeNavBar]의 탭 항목
 */
@Composable
fun RouteeNavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) RouteeTheme.colors.white else RouteeTheme.colors.grey400

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius24))
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

@PreviewFontSizes
@Composable
private fun RouteeNavBarPreview() {
    RouteeTheme {
        RouteeNavBar(
            selectedIndex = 0,
            modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 8.dp),
        ) {
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
