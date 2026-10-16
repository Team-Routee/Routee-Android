package com.routee.android.core.ui.error.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val ButtonWidth = 240.dp

@Composable
internal fun ErrorButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Full)
    val contentColor = if (isPrimary) RouteeTheme.colors.mint500 else RouteeTheme.colors.grey200
    val borderColor = if (isPrimary) RouteeTheme.colors.mint500 else RouteeTheme.colors.grey50
    val backgroundColor = if (isPrimary) RouteeTheme.colors.bgCtaSecondary else Color.Transparent

    Row(
        modifier = modifier
            .width(ButtonWidth)
            .background(backgroundColor, shape)
            .border(RouteeTheme.border.Thickness, borderColor, shape)
            .noRippleClickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(vertical = RouteeTheme.spacing.Spacing16),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Image(
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
            )
        }
        Text(
            text = text,
            style = RouteeTheme.typography.labelSb16,
            color = contentColor,
        )
    }
}

@PreviewFontSizes
@Composable
private fun ErrorButtonPreview() {
    RouteePreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16)) {
            ErrorButton(
                text = "다시 시도",
                onClick = {},
                isPrimary = true,
                iconRes = DesignSystemR.drawable.ic_retry,
            )
            ErrorButton(
                text = "홈으로 이동",
                onClick = {},
                isPrimary = false,
            )
        }
    }
}
