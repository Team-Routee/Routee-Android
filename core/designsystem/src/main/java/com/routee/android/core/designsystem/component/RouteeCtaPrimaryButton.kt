package com.routee.android.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val CtaMinHeight = 52.dp

/**
 * 화면 하단 메인 버튼
 *
 * 누르는 동안 밝은 색으로 바뀌고, [enabled]가 false면 회색으로 표시되며 클릭되지 않는다
 */
@Composable
fun RouteeCtaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = RouteeTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        !enabled -> colors.grey600
        isPressed -> colors.mint200
        else -> colors.bgCtaPrimary
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = CtaMinHeight)
            .clip(RoundedCornerShape(RouteeTheme.radius.Full))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = RouteeTheme.spacing.Spacing16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = RouteeTheme.typography.labelSb16,
            color = if (enabled) colors.black else colors.grey200,
        )
    }
}

private class RouteeCtaPrimaryButtonPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@Preview(widthDp = 360)
@Composable
private fun RouteeCtaPrimaryButtonPreview(
    @PreviewParameter(RouteeCtaPrimaryButtonPreviewParameterProvider::class) enabled: Boolean,
) {
    RouteePreviewTheme {
        RouteeCtaPrimaryButton(
            text = "다음",
            onClick = {},
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
