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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

private val LocationIconSize = 24.dp

/**
 * 현재 위치 칩 (chip_location_main)
 *
 * 위치 아이콘 옆에 주소를 한 줄로 보여준다. 길면 말줄임 처리한다
 *
 * @param hasLocationPermission false면 [address] 대신 권한 안내 문구를 보여준다
 */
@Composable
internal fun LocationChip(
    address: String,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius16))
            .background(RouteeTheme.colors.grey500)
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
            text = if (hasLocationPermission) {
                address
            } else {
                stringResource(
                    R.string.workout_main_location_permission_required,
                )
            },
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
            LocationChip(address = "영등포구 의사당대로 1", hasLocationPermission = true)
            LocationChip(address = "", hasLocationPermission = false)
        }
    }
}
