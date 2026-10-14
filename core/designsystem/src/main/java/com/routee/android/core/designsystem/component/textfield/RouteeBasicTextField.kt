package com.routee.android.core.designsystem.component.textfield

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.common.extension.graphemeLength
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 디자인 없이 동작만 담당하는 한 줄 입력창
 *
 * 테두리, 배경, 상태별 색은 이 컴포넌트를 감싸는 쪽에서 그린다. 포커스 여부가 필요하면 [interactionSource]를 넘겨 관찰한다
 *
 * @param state 입력값. 호출하는 쪽에서 rememberTextFieldState 또는 ViewModel의 [TextFieldState]를 넘긴다
 * @param placeholder 입력값이 비어 있을 때 표시할 문구
 * @param maxLength 최대 글자 수 넘는 입력은 막는다
 * @param leadingContent 입력창 앞 아이콘 자리. 눌러도 입력창에 포커스된다
 * @param trailingContent 입력창 뒤 아이콘 자리. 눌러도 입력창에 포커스된다
 */
@Composable
fun RouteeBasicTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = RouteeTheme.typography.bodyR16,
    textColor: Color = RouteeTheme.colors.white,
    placeholder: String? = null,
    placeholderColor: Color = RouteeTheme.colors.white30,
    maxLength: Int? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    interactionSource: MutableInteractionSource? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    BasicTextField(
        state = state,
        modifier = modifier,
        enabled = enabled,
        inputTransformation = maxLength?.let { MaxGraphemeLengthTransformation(it) },
        textStyle = textStyle.copy(color = textColor),
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = TextFieldLineLimits.SingleLine,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(textColor),
        decorator = TextFieldDecorator { innerTextField ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingContent?.invoke()
                Box(modifier = Modifier.weight(1f)) {
                    if (placeholder != null && state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle,
                            color = placeholderColor,
                        )
                    }
                    innerTextField()
                }
                trailingContent?.invoke()
            }
        },
    )
}

private class MaxGraphemeLengthTransformation(
    private val maxLength: Int,
) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (asCharSequence().graphemeLength() > maxLength) revertAllChanges()
    }
}

private class RouteeBasicTextFieldPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("", "관악산 날다람쥐")
}

@Preview(widthDp = 360)
@Composable
private fun RouteeBasicTextFieldPreview(
    @PreviewParameter(RouteeBasicTextFieldPreviewParameterProvider::class) text: String,
) {
    RouteePreviewTheme {
        RouteeBasicTextField(
            state = rememberTextFieldState(text),
            placeholder = "닉네임을 입력해주세요",
            modifier = Modifier
                .fillMaxWidth()
                .padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
