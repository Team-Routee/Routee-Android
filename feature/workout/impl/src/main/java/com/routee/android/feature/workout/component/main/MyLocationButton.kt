package com.routee.android.feature.workout.component.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

private val ButtonSize = 44.dp

/**
 * 내 위치 버튼 (fab_inplace_main)
 *
 * @param onClick 탭 시 호출
 */
@Composable
internal fun MyLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_inplace),
        contentDescription = stringResource(R.string.workout_main_my_location_a11y),
        modifier = modifier
            .size(ButtonSize)
            .clip(CircleShape)
            .background(RouteeTheme.colors.bgPrimary)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(RouteeTheme.spacing.Spacing8),
    )
}

@PreviewFontSizes
@Composable
private fun MyLocationButtonPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        MyLocationButton(onClick = {})
    }
}
