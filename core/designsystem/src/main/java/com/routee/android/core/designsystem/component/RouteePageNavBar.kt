package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 뒤로 가기 버튼이 있는 상단 바
 *
 * @param title 가운데 페이지 제목. null이면 표시하지 않음
 * @param actions 우측 영역. 완료, 확인, 삭제 같은 텍스트 버튼을 넣는다
 */
@Composable
fun RouteePageNavBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RouteeTheme.spacing.Spacing4),
    ) {
        BackButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        if (title != null) {
            Text(
                text = title,
                style = RouteeTheme.typography.labelSb18,
                color = RouteeTheme.colors.white,
                modifier = Modifier
                    .align(Alignment.Center)
                    .semantics { heading() },
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(R.drawable.ic_arrow_left_sm_white),
        contentDescription = stringResource(R.string.designsystem_page_nav_bar_back_a11y),
        tint = RouteeTheme.colors.white,
        modifier = modifier
            .noRippleClickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(RouteeTheme.spacing.Spacing10),
    )
}

private enum class RouteePageNavBarPreviewType {
    BackOnly,
    Title,
    Finish,
    Delete,
}

private class RouteePageNavBarPreviewParameterProvider : PreviewParameterProvider<RouteePageNavBarPreviewType> {
    override val values = RouteePageNavBarPreviewType.entries.asSequence()
}

@Preview(widthDp = 360)
@Composable
private fun RouteePageNavBarPreview(
    @PreviewParameter(RouteePageNavBarPreviewParameterProvider::class) type: RouteePageNavBarPreviewType,
) {
    RouteePreviewTheme {
        RouteePageNavBar(
            onBackClick = {},
            title = "페이지 제목".takeIf { type == RouteePageNavBarPreviewType.Title },
            actions = {
                when (type) {
                    RouteePageNavBarPreviewType.Finish -> Text(
                        text = "완료",
                        style = RouteeTheme.typography.labelSb18,
                        color = RouteeTheme.colors.white,
                        modifier = Modifier
                            .noRippleClickable(onClick = {})
                            .semantics { role = Role.Button }
                            .padding(horizontal = RouteeTheme.spacing.Spacing16),
                    )

                    RouteePageNavBarPreviewType.Delete -> Text(
                        text = "삭제",
                        style = RouteeTheme.typography.labelSb18,
                        color = RouteeTheme.colors.statusError,
                        modifier = Modifier
                            .noRippleClickable(onClick = {})
                            .semantics { role = Role.Button }
                            .padding(horizontal = RouteeTheme.spacing.Spacing16),
                    )

                    else -> Unit
                }
            },
        )
    }
}
