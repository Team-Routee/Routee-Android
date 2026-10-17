package com.routee.android.core.designsystem.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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

// modal_action_vertical_android
/**
 * 선택 다이얼로그 버튼
 *
 * @param text     버튼 문구
 * @param onClick  탭 시 호출. 닫기는 호출하는 쪽에서
 */
@Immutable
data class RouteeDialogAction(
    val text: String,
    val onClick: () -> Unit,
)

/**
 * 선택 다이얼로그
 *
 * @param title             제목
 * @param actions           제목 아래 세로로 쌓을 버튼 목록
 * @param onDismissRequest  닫기 버튼, 뒤로가기, 바깥 영역 탭 시 호출
 */
@Composable
fun RouteeVerticalActionDialog(
    title: String,
    actions: List<RouteeDialogAction>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeDialogContainer(
        onDismissRequest = onDismissRequest,
        contentPadding = VerticalActionDialogPadding,
        modifier = modifier,
    ) {
        VerticalActionDialogContent(
            title = title,
            actions = actions,
            onDismissRequest = onDismissRequest,
        )
    }
}

private val VerticalActionDialogPadding = PaddingValues(
    start = RouteeSpacing.Spacing24,
    top = RouteeSpacing.Spacing20,
    end = RouteeSpacing.Spacing24,
    bottom = RouteeSpacing.Spacing24,
)

@Composable
private fun ColumnScope.VerticalActionDialogContent(
    title: String,
    actions: List<RouteeDialogAction>,
    onDismissRequest: () -> Unit,
) {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.ic_cancel_sm_outline),
        contentDescription = stringResource(R.string.designsystem_dialog_close_a11y),
        modifier = Modifier
            .align(Alignment.End)
            .background(color = RouteeTheme.colors.white10, shape = CircleShape)
            .noRippleClickable(role = Role.Button, onClick = onDismissRequest),
    )
    // info
    Text(
        text = title,
        style = RouteeTheme.typography.titleSb18,
        color = RouteeTheme.colors.white,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(
            start = RouteeTheme.spacing.Spacing16,
            end = RouteeTheme.spacing.Spacing16,
            bottom = RouteeTheme.spacing.Spacing16,
        ),
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing10),
    ) {
        actions.forEach { action ->
            DialogActionButton(
                text = action.text,
                onClick = action.onClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteeVerticalActionDialogPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(RouteeTheme.spacing.Spacing16)) {
        DialogCard(contentPadding = VerticalActionDialogPadding) {
            VerticalActionDialogContent(
                title = "프로필 사진 설정",
                actions = listOf(
                    RouteeDialogAction(text = "앨범에서 선택", onClick = {}),
                    RouteeDialogAction(text = "기본 이미지 적용", onClick = {}),
                ),
                onDismissRequest = {},
            )
        }
    }
}
