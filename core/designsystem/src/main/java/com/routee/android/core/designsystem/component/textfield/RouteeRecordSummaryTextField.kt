package com.routee.android.core.designsystem.component.textfield

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private const val RECORD_TITLE_MAX_LENGTH = 16

/**
 * 기록 제목 헤더
 *
 * 연필 아이콘을 누르면 같은 자리에서 제목을 편집한다. 키보드 완료나 포커스 해제 시 [onEditComplete]가 호출된다. 최대 16자까지 입력된다
 *
 * @param isEditing 편집 중 여부. [onEditClick]에서 true로, [onEditComplete]에서 false로 바꾼다
 */
@Composable
fun RouteeRecordSummaryTextField(
    state: TextFieldState,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onEditComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isEditing) {
            RecordTitleEditor(
                state = state,
                onEditComplete = onEditComplete,
                modifier = Modifier.weight(1f),
            )
        } else {
            Text(
                text = state.text.toString(),
                style = RouteeTheme.typography.titleSb20,
                color = RouteeTheme.colors.white,
                modifier = Modifier.weight(1f),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_edit_sm_line_white),
            contentDescription = stringResource(R.string.designsystem_record_summary_text_field_edit_a11y),
            tint = Color.Unspecified,
            modifier = Modifier
                .noRippleClickable(onClick = onEditClick)
                .semantics { role = Role.Button }
                .padding(RouteeTheme.spacing.Spacing10),
        )
    }
}

@Composable
private fun RecordTitleEditor(
    state: TextFieldState,
    onEditComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val currentOnEditComplete by rememberUpdatedState(onEditComplete)
    var wasFocused by remember { mutableStateOf(false) }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    RouteeBasicTextField(
        state = state,
        textStyle = RouteeTheme.typography.titleSb20,
        maxLength = RECORD_TITLE_MAX_LENGTH,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        onKeyboardAction = { focusManager.clearFocus() },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (wasFocused && !focusState.isFocused) currentOnEditComplete()
                wasFocused = focusState.isFocused
            },
    )
}

private class RouteeRecordSummaryTextFieldPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview(widthDp = 360)
@Composable
private fun RouteeRecordSummaryTextFieldPreview(
    @PreviewParameter(RouteeRecordSummaryTextFieldPreviewParameterProvider::class) isEditing: Boolean,
) {
    RouteePreviewTheme {
        RouteeRecordSummaryTextField(
            state = rememberTextFieldState("2026.07.18 기록"),
            isEditing = isEditing,
            onEditClick = {},
            onEditComplete = {},
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
