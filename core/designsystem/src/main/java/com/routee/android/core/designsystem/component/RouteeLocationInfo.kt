package com.routee.android.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 지도 핀 아이콘과 위치명을 표시하는 라벨
 *
 * @param location 표시할 위치명
 */
@Composable
fun RouteeLocationInfo(
    location: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = RouteeTheme.colors.dimSecondary,
                shape = RoundedCornerShape(RouteeTheme.radius.Radius8),
            )
            .padding(
                start = RouteeTheme.spacing.Spacing4,
                top = RouteeTheme.spacing.Spacing2,
                end = RouteeTheme.spacing.Spacing8,
                bottom = RouteeTheme.spacing.Spacing2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_location_info_sm_gradient),
            contentDescription = null,
        )
        Text(
            text = location,
            color = RouteeTheme.colors.mint100,
            style = RouteeTheme.typography.labelM14,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewFontSizes
@Composable
private fun RouteeLocationInfoPreview() {
    RouteeTheme {
        RouteeLocationInfo(location = "창의문")
    }
}
