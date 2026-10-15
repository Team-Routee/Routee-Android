package com.routee.android.feature.workout.component.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val LocationIconSize = 24.dp

/**
 * 현재 위치 칩 (chip_location_main)
 *
 * 위치 아이콘 옆에 문구를 한 줄로 보여준다. 길면 말줄임 처리한다
 *
 * @param text 주소, 권한 안내 같은 표시할 문구. 어떤 문구를 보여줄지는 호출하는 화면이 상태를 보고 정한다
 */
@Composable
internal fun LocationChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = RouteeTheme.colors.grey500,
                shape = RoundedCornerShape(RouteeTheme.radius.Radius16),
            )
            .padding(
                start = RouteeTheme.spacing.Spacing8,
                end = RouteeTheme.spacing.Spacing12,
                top = RouteeTheme.spacing.Spacing4,
                bottom = RouteeTheme.spacing.Spacing4,
            ),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_location_info_sm_gradient),
            contentDescription = null,
            modifier = Modifier.size(LocationIconSize),
        )
        Text(
            text = text,
            style = RouteeTheme.typography.labelM12,
            color = RouteeTheme.colors.grey50,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewFontSizes
@Composable
private fun LocationChipPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LocationChip(text = "영등포구 의사당대로 1")
            LocationChip(text = "위치 권한 허용이 필요합니다.")
        }
    }
}
