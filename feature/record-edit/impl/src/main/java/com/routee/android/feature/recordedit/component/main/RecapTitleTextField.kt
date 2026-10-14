package com.routee.android.feature.recordedit.component.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.textfield.RouteeBasicTextField
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R

private const val RECAP_TITLE_MAX_LENGTH = 16

/**
 * 기록 카드의 제목 영역 (textfield_recap_title)
 *
 * @param title 저장된 제목. 편집 중이 아닐 때 표시한다
 * @param editingState 편집 중인 제목 상태. null이면 편집 중이 아니다
 * @param onEditClick 연필 아이콘 클릭 시 호출. 호출하는 쪽에서 [editingState]를 만들어 넘긴다
 * @param onEditComplete 키보드 완료 버튼 클릭 시 호출
 */
@Composable
internal fun RecapTitleTextField(
    title: String,
    editingState: TextFieldState?,
    onEditClick: () -> Unit,
    onEditComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (editingState == null) {
        RecapTitleDisplay(
            title = title,
            onEditClick = onEditClick,
            modifier = modifier,
        )
    } else {
        RecapTitleEditor(
            state = editingState,
            onEditComplete = onEditComplete,
            modifier = modifier,
        )
    }
}

@Composable
private fun RecapTitleDisplay(
    title: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editDescription = stringResource(R.string.record_edit_main_recap_title_edit_a11y)

    Row(
        modifier = modifier.padding(
            horizontal = RouteeTheme.spacing.Spacing4,
            vertical = RouteeTheme.spacing.Spacing2,
        ),
        horizontalArrangement = Arrangement.spacedBy(
            space = RouteeTheme.spacing.Spacing2,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = RouteeTheme.typography.bodySb14,
            color = RouteeTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_edit_sm_line_grey),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .noRippleClickable(onClick = onEditClick)
                .semantics {
                    role = Role.Button
                    contentDescription = editDescription
                },
        )
    }
}

@Composable
private fun RecapTitleEditor(
    state: TextFieldState,
    onEditComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val underlineColor = RouteeTheme.colors.statusInfo

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    RouteeBasicTextField(
        state = state,
        textStyle = RouteeTheme.typography.bodySb14,
        maxLength = RECAP_TITLE_MAX_LENGTH,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        onKeyboardAction = { onEditComplete() },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .drawBehind {
                val strokeWidth = RouteeTheme.border.Thickness.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = underlineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(
                horizontal = RouteeTheme.spacing.Spacing4,
                vertical = RouteeTheme.spacing.Spacing2,
            ),
    )
}

private class RecapTitleTextFieldPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview(widthDp = 144)
@Composable
private fun RecapTitleTextFieldPreview(
    @PreviewParameter(RecapTitleTextFieldPreviewParameterProvider::class) isEditing: Boolean,
) {
    val title = "숭실대 동기모임 북악산 등산"
    val editingState = rememberTextFieldState(title)

    RouteePreviewTheme {
        RecapTitleTextField(
            title = title,
            editingState = if (isEditing) editingState else null,
            onEditClick = {},
            onEditComplete = {},
        )
    }
}
