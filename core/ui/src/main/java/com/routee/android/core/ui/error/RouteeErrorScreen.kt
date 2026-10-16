package com.routee.android.core.ui.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.R
import com.routee.android.core.ui.error.component.ErrorButton
private const val CONTENT_TOP_RATIO = 201f / 760f
private val ContentToButtonSpacing = 80.dp

/**
 * 에러 화면
 *
 * 데이터 조회 실패 시 화면 내용 대신 보여준다. [RouteeErrorType.Unknown]이고 [onRetryClick]이 있으면 [다시 시도] 버튼을 함께 보여준다
 *
 * @param onRetryClick 직전 요청을 다시 보낸다
 */
@Composable
fun RouteeErrorScreen(
    type: RouteeErrorType,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryClick: (() -> Unit)? = null,
) {
    val showRetry = type == RouteeErrorType.Unknown && onRetryClick != null

    RouteeBgEllipse(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.statusBarsPadding()) {
            val contentTopPadding = maxHeight * CONTENT_TOP_RATIO

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(contentTopPadding))
                Image(
                    imageVector = ImageVector.vectorResource(type.graphicRes),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing24))
                Text(
                    text = stringResource(type.titleRes),
                    style = RouteeTheme.typography.labelM16,
                    color = RouteeTheme.colors.white,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing10))
                Text(
                    text = stringResource(type.descriptionRes),
                    style = RouteeTheme.typography.labelR12,
                    color = RouteeTheme.colors.grey200,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(ContentToButtonSpacing))
                Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16)) {
                    if (showRetry) {
                        ErrorButton(
                            text = stringResource(R.string.ui_error_retry_button),
                            onClick = onRetryClick,
                            isPrimary = true,
                            iconRes = DesignSystemR.drawable.ic_retry,
                        )
                    }
                    ErrorButton(
                        text = stringResource(R.string.ui_error_home_button),
                        onClick = onHomeClick,
                        isPrimary = !showRetry,
                    )
                }
            }
        }
    }
}

private class RouteeErrorTypePreviewParameterProvider : PreviewParameterProvider<RouteeErrorType> {
    override val values = RouteeErrorType.entries.asSequence()
}

@PreviewScreenSizes
@Composable
private fun RouteeErrorScreenPreview(
    @PreviewParameter(RouteeErrorTypePreviewParameterProvider::class) type: RouteeErrorType,
) {
    RouteePreviewTheme {
        RouteeErrorScreen(
            type = type,
            onHomeClick = {},
            onRetryClick = {},
        )
    }
}
