package com.routee.android.core.designsystem.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeSpacing
import com.routee.android.core.designsystem.theme.RouteeTheme

// modal_draft_primary_android

/**
 * 이전 기록 이어가기 다이얼로그. 뒤로가기, 바깥 영역 탭으로 닫히지 않음
 *
 * @param title                   제목
 * @param description             제목 아래 설명 (예: "03/03 오전 3:33 까지 기록")
 * @param metrics                 설명 아래 "·"로 잇는 값 (예: "4.28km", "1h 20m")
 * @param secondaryButtonText     왼쪽 버튼 문구
 * @param primaryButtonText       오른쪽 강조 버튼 문구
 * @param onSecondaryButtonClick  왼쪽 버튼 탭 시 호출
 * @param onPrimaryButtonClick    오른쪽 버튼 탭 시 호출
 * @param onCloseClick            닫기 버튼 탭 시 호출
 */
@Composable
fun RouteeDraftDialog(
    title: String,
    description: String,
    metrics: List<String>,
    secondaryButtonText: String,
    primaryButtonText: String,
    onSecondaryButtonClick: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeDialogContainer(
        onDismissRequest = {},
        contentPadding = DraftDialogPadding,
        modifier = modifier,
        dismissOnDimClick = false,
    ) {
        DraftDialogContent(
            title = title,
            description = description,
            metrics = metrics,
            secondaryButtonText = secondaryButtonText,
            primaryButtonText = primaryButtonText,
            onSecondaryButtonClick = onSecondaryButtonClick,
            onPrimaryButtonClick = onPrimaryButtonClick,
            onCloseClick = onCloseClick,
        )
    }
}

private val DraftDialogPadding = PaddingValues(
    start = RouteeSpacing.Spacing24,
    top = RouteeSpacing.Spacing14,
    end = RouteeSpacing.Spacing24,
    bottom = RouteeSpacing.Spacing24,
)

@Composable
private fun ColumnScope.DraftDialogContent(
    title: String,
    description: String,
    metrics: List<String>,
    secondaryButtonText: String,
    primaryButtonText: String,
    onSecondaryButtonClick: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.ic_cancel_sm_outline),
        contentDescription = stringResource(R.string.designsystem_dialog_close_a11y),
        modifier = Modifier
            .align(Alignment.End)
            .background(color = RouteeTheme.colors.white10, shape = CircleShape)
            .noRippleClickable(role = Role.Button, onClick = onCloseClick),
    )
    // info
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = RouteeTheme.spacing.Spacing8,
                top = RouteeTheme.spacing.Spacing8,
                end = RouteeTheme.spacing.Spacing8,
                bottom = RouteeTheme.spacing.Spacing16,
            ),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = RouteeTheme.typography.titleSb18,
            color = RouteeTheme.colors.white,
            textAlign = TextAlign.Center,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = description,
                style = RouteeTheme.typography.labelM14,
                color = RouteeTheme.colors.grey300,
                textAlign = TextAlign.Center,
            )
            DraftMetrics(metrics = metrics)
        }
    }
    // action_group
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
    ) {
        DialogActionButton(
            text = secondaryButtonText,
            onClick = onSecondaryButtonClick,
            modifier = Modifier.weight(1f),
            contentColor = RouteeTheme.colors.white60,
        )
        DialogActionButton(
            text = primaryButtonText,
            onClick = onPrimaryButtonClick,
            modifier = Modifier.weight(1f),
            contentColor = RouteeTheme.colors.black,
            containerColor = RouteeTheme.colors.bgCtaPrimary,
        )
    }
}

@Composable
private fun DraftMetrics(metrics: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8)) {
        metrics.forEachIndexed { index, metric ->
            if (index > 0) {
                Text(
                    text = METRIC_SEPARATOR,
                    style = RouteeTheme.typography.titleSb18,
                    color = RouteeTheme.colors.white60,
                )
            }
            Text(
                text = metric,
                style = RouteeTheme.typography.titleSb18,
                color = RouteeTheme.colors.white,
            )
        }
    }
}

private const val METRIC_SEPARATOR = "·"

@PreviewFontSizes
@Composable
private fun RouteeDraftDialogPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(RouteeTheme.spacing.Spacing16)) {
        DialogCard(contentPadding = DraftDialogPadding) {
            DraftDialogContent(
                title = "이전 기록을 이어갈까요?",
                description = "03/03 오전 3:33 까지 기록",
                metrics = listOf("4.28km", "1h 20m"),
                secondaryButtonText = "종료 후 저장",
                primaryButtonText = "이어서 기록",
                onSecondaryButtonClick = {},
                onPrimaryButtonClick = {},
                onCloseClick = {},
            )
        }
    }
}
