package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val TextButtonMinHeight = 42.dp

/**
 * 배경 없는 텍스트 버튼의 종류
 */
enum class RouteeTextButtonRole {
    Default,
    Destructive,
}

/**
 * 배경 없는 텍스트 버튼
 *
 * 삭제처럼 되돌릴 수 없는 동작은 [RouteeTextButtonRole.Destructive]로 빨간 글자로 표시
 */
@Composable
fun RouteeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: RouteeTextButtonRole = RouteeTextButtonRole.Default,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = TextButtonMinHeight)
            .noRippleClickable(onClick = onClick)
            .semantics { this.role = Role.Button }
            .padding(horizontal = RouteeTheme.spacing.Spacing16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = RouteeTheme.typography.labelSb18,
            color = when (role) {
                RouteeTextButtonRole.Default -> RouteeTheme.colors.white
                RouteeTextButtonRole.Destructive -> RouteeTheme.colors.statusError
            },
        )
    }
}

private class RouteeTextButtonPreviewParameterProvider : PreviewParameterProvider<RouteeTextButtonRole> {
    override val values = RouteeTextButtonRole.entries.asSequence()
}

@Preview
@Composable
private fun RouteeTextButtonPreview(
    @PreviewParameter(RouteeTextButtonPreviewParameterProvider::class) role: RouteeTextButtonRole,
) {
    RouteePreviewTheme {
        RouteeTextButton(
            text = "버튼",
            onClick = {},
            role = role,
        )
    }
}
