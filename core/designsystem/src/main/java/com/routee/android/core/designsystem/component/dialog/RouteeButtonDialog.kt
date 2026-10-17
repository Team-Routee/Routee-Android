package com.routee.android.core.designsystem.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

// modal_action_primary_android (actionCount=single)

/**
 * 버튼 하나 다이얼로그
 *
 * @param title             제목
 * @param buttonText        버튼 문구 (예: "확인")
 * @param onButtonClick     버튼 탭 시 호출. 닫기는 호출하는 쪽에서
 * @param onDismissRequest  뒤로가기, 바깥 영역 탭 시 호출
 * @param description       제목 아래 설명. null 이면 숨김
 */
@Composable
fun RouteeOneButtonDialog(
    title: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    RouteeDialogContainer(
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(RouteeTheme.spacing.Spacing24),
        modifier = modifier,
    ) {
        OneButtonDialogContent(
            title = title,
            buttonText = buttonText,
            onButtonClick = onButtonClick,
            description = description,
        )
    }
}

// modal_action_primary_android (actionCount=double)

/**
 * 버튼 두 개 다이얼로그. 오른쪽 버튼은 빨간 글자
 *
 * @param title               제목
 * @param leftButtonText      왼쪽 버튼 문구 (예: "취소")
 * @param rightButtonText     오른쪽 버튼 문구 (예: "기록 삭제")
 * @param onRightButtonClick  오른쪽 버튼 탭 시 호출. 닫기는 호출하는 쪽에서
 * @param onDismissRequest    왼쪽 버튼, 뒤로가기, 바깥 영역 탭 시 호출
 * @param description         제목 아래 설명. null 이면 숨김
 */
@Composable
fun RouteeTwoButtonDialog(
    title: String,
    leftButtonText: String,
    rightButtonText: String,
    onRightButtonClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    RouteeDialogContainer(
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(RouteeTheme.spacing.Spacing24),
        modifier = modifier,
    ) {
        TwoButtonDialogContent(
            title = title,
            leftButtonText = leftButtonText,
            rightButtonText = rightButtonText,
            onLeftButtonClick = onDismissRequest,
            onRightButtonClick = onRightButtonClick,
            description = description,
        )
    }
}

@Composable
private fun ColumnScope.OneButtonDialogContent(
    title: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    description: String?,
) {
    DialogInfo(title = title, description = description)
    DialogActionButton(
        text = buttonText,
        onClick = onButtonClick,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ColumnScope.TwoButtonDialogContent(
    title: String,
    leftButtonText: String,
    rightButtonText: String,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit,
    description: String?,
) {
    DialogInfo(title = title, description = description)
    // action_group
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
    ) {
        DialogActionButton(
            text = leftButtonText,
            onClick = onLeftButtonClick,
            modifier = Modifier.weight(1f),
        )
        DialogActionButton(
            text = rightButtonText,
            onClick = onRightButtonClick,
            modifier = Modifier.weight(1f),
            isDestructive = true,
        )
    }
}

// info
@Composable
private fun DialogInfo(
    title: String,
    description: String?,
) {
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
        if (description != null) {
            Text(
                text = description,
                style = RouteeTheme.typography.labelM14,
                color = RouteeTheme.colors.grey300,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteeOneButtonDialogPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(RouteeTheme.spacing.Spacing16)) {
        DialogCard(contentPadding = PaddingValues(RouteeTheme.spacing.Spacing24)) {
            OneButtonDialogContent(
                title = "기록 등록 실패",
                buttonText = "확인",
                onButtonClick = {},
                description = "인터넷 연결이 불안정해서\n기록이 등록되지 않았어요.\n연결이 안정되면 다시 기록해주세요.",
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteeTwoButtonDialogPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(RouteeTheme.spacing.Spacing16)) {
        Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16)) {
            DialogCard(contentPadding = PaddingValues(RouteeTheme.spacing.Spacing24)) {
                TwoButtonDialogContent(
                    title = "기록을 삭제하시겠습니까?",
                    leftButtonText = "취소",
                    rightButtonText = "기록 삭제",
                    onLeftButtonClick = {},
                    onRightButtonClick = {},
                    description = "나가면 운동 기록이 삭제되고\n복구할 수 없습니다.",
                )
            }
            DialogCard(contentPadding = PaddingValues(RouteeTheme.spacing.Spacing24)) {
                TwoButtonDialogContent(
                    title = "기록을 삭제하시겠습니까?",
                    leftButtonText = "취소",
                    rightButtonText = "기록 삭제",
                    onLeftButtonClick = {},
                    onRightButtonClick = {},
                    description = null,
                )
            }
        }
    }
}
