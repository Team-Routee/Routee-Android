package com.routee.android.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 이미지 회전 버튼 묶음 (왼쪽 90도 회전 / 회전 초기화 / 오른쪽 90도 회전)
 */
@Composable
fun RouteeImageRotationController(
    onRotateLeftClick: () -> Unit,
    onResetClick: () -> Unit,
    onRotateRightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(RouteeTheme.colors.black, RoundedCornerShape(RouteeTheme.radius.Full))
            .padding(
                horizontal = RouteeTheme.spacing.Spacing12,
                vertical = RouteeTheme.spacing.Spacing4,
            ),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing18),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RotationButton(
            iconRes = R.drawable.ic_270_degree_rotation_white_lg,
            contentDescription = stringResource(R.string.designsystem_image_rotation_left_a11y),
            onClick = onRotateLeftClick,
        )
        RotationButton(
            iconRes = R.drawable.ic_reset_rotation_white_lg,
            contentDescription = stringResource(R.string.designsystem_image_rotation_reset_a11y),
            onClick = onResetClick,
        )
        RotationButton(
            iconRes = R.drawable.ic_90_degree_rotation_white_lg,
            contentDescription = stringResource(R.string.designsystem_image_rotation_right_a11y),
            onClick = onRotateRightClick,
        )
    }
}

@Composable
private fun RotationButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = Color.Unspecified,
        modifier = modifier
            .noRippleClickable(onClick = onClick)
            .semantics { role = Role.Button },
    )
}

@Preview
@Composable
private fun RouteeImageRotationControllerPreview() {
    RouteePreviewTheme {
        RouteeImageRotationController(
            onRotateLeftClick = {},
            onResetClick = {},
            onRotateRightClick = {},
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
