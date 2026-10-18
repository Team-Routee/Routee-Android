package com.routee.android.core.designsystem.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private const val LOCATION_MAX_LENGTH = 16

/**
 * 위치 입력창
 *
 * 포커스 중에는 파란 테두리로 표시되고, 아래에 글자 수가 표시된다. 최대 16자까지 입력된다
 */
@Composable
fun RouteeLocationTextField(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val status = RouteeTextFieldStatus.from(isFocused = isFocused, validation = RouteeTextFieldValidation.None)
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius16)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
        horizontalAlignment = Alignment.End,
    ) {
        RouteeBasicTextField(
            state = state,
            textStyle = RouteeTheme.typography.labelM14,
            placeholder = placeholder,
            placeholderColor = RouteeTheme.colors.grey300,
            maxLength = LOCATION_MAX_LENGTH,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = onKeyboardAction,
            interactionSource = interactionSource,
            leadingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_location_info_sm_mint),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(RouteeTheme.colors.black60, shape)
                .border(RouteeTheme.border.Thickness, status.borderColor, shape)
                .padding(RouteeTheme.spacing.Spacing16),
        )
        RouteeTextFieldLengthCounter(state = state, maxLength = LOCATION_MAX_LENGTH)
    }
}

private class RouteeLocationTextFieldPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("", "관악산 말바위 전망 포토 스팟")
}

@Preview(widthDp = 360)
@Composable
private fun RouteeLocationTextFieldPreview(
    @PreviewParameter(RouteeLocationTextFieldPreviewParameterProvider::class) text: String,
) {
    RouteePreviewTheme {
        RouteeLocationTextField(
            state = rememberTextFieldState(text),
            placeholder = "현재 위치를 남겨주세요.",
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
