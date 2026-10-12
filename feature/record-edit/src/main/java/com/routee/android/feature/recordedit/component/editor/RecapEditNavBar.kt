package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapEditTab

/**
 * 기록 편집 하단 편집 탭 바 (recap_nav_bar)
 *
 * @param selectedTab 선택된 탭. 최초 진입 시에는 모든 탭이 미선택이므로 null
 * @param onTabClick 탭 클릭 시 호출. 같은 탭 재클릭 시 해제 여부는 호출하는 쪽에서 결정
 */
@Composable
internal fun RecapEditNavBar(
    selectedTab: RecapEditTab?,
    onTabClick: (RecapEditTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RouteeTheme.colors.black80)
            .padding(horizontal = RouteeTheme.spacing.Spacing16)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecapEditTab.entries.forEach { tab ->
            RecapEditTabItem(
                tab = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabClick(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private class RecapEditNavBarPreviewParameterProvider : PreviewParameterProvider<RecapEditTab?> {
    override val values = sequenceOf(null) + RecapEditTab.entries.asSequence()
}

@Preview(widthDp = 360)
@Composable
private fun RecapEditNavBarPreview(
    @PreviewParameter(RecapEditNavBarPreviewParameterProvider::class) selectedTab: RecapEditTab?,
) {
    RouteePreviewTheme {
        RecapEditNavBar(
            selectedTab = selectedTab,
            onTabClick = {},
        )
    }
}
