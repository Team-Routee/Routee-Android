package com.routee.android.core.designsystem.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.contextmenu.data.TextContextMenuKeys
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private const val NICKNAME_MAX_LENGTH = 12

/**
 * 닉네임 입력창
 *
 * 테두리 색과 우측 아이콘은 포커스 여부와 [validation]으로 정해진다. 최대 12자까지 입력되며 길게 눌러도 붙여넣기 메뉴는 나오지 않는다
 *
 * @param guideText 입력창 아래 안내 문구. [RouteeTextFieldValidation.Invalid]면 빨간색으로 표시
 * @param showEditIcon 포커스 없고 검증 전일 때 우측에 편집 아이콘 표시 여부. 설정 화면에서 사용
 */
@Composable
fun RouteeNicknameTextField(
    state: TextFieldState,
    placeholder: String,
    guideText: String,
    modifier: Modifier = Modifier,
    validation: RouteeTextFieldValidation = RouteeTextFieldValidation.None,
    onKeyboardAction: KeyboardActionHandler? = null,
    showEditIcon: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val status = RouteeTextFieldStatus.from(isFocused = isFocused, validation = validation)
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius12)
    val trailingIconRes = if (showEditIcon && status == RouteeTextFieldStatus.Initial) {
        R.drawable.ic_edit_sm_line_white
    } else {
        status.trailingIconRes
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6),
    ) {
        RouteeBasicTextField(
            state = state,
            placeholder = placeholder,
            maxLength = NICKNAME_MAX_LENGTH,
            inputTransformation = NicknameSpaceTransformation,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = onKeyboardAction,
            interactionSource = interactionSource,
            trailingContent = trailingIconRes?.let { iconRes ->
                {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = RouteeTheme.field.MinHeight)
                .background(RouteeTheme.colors.black60, shape)
                .border(RouteeTheme.border.Thickness, status.borderColor, shape)
                .padding(horizontal = RouteeTheme.spacing.Spacing12)
                .filterTextContextMenuComponents { it.key != TextContextMenuKeys.PasteKey }
                .semantics { if (status == RouteeTextFieldStatus.Error) error(guideText) },
        )
        Text(
            text = guideText,
            style = RouteeTheme.typography.labelR12,
            color = status.feedbackColor,
        )
    }
}

/**
 * 앞 공백, 연속 공백을 넣는 입력을 막는다 (RT-AUTH-003 입력 규칙)
 *
 * 글자를 지워서 생기는 연속 공백은 막지 않는다. 막으면 단어 사이 글자를 지울 수 없다
 */
internal object NicknameSpaceTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (length <= originalText.length) return
        if (asCharSequence().spaceViolationCount() > originalText.spaceViolationCount()) revertAllChanges()
    }

    private fun CharSequence.spaceViolationCount(): Int {
        val leadingSpace = if (firstOrNull()?.isWhitespace() == true) 1 else 0
        val consecutiveSpaces = zipWithNext().count { (a, b) -> a.isWhitespace() && b.isWhitespace() }
        return leadingSpace + consecutiveSpaces
    }
}

@Preview(widthDp = 360)
@Composable
private fun RouteeNicknameTextFieldEditIconPreview() {
    RouteePreviewTheme {
        RouteeNicknameTextField(
            state = rememberTextFieldState("관악산 날다람쥐"),
            placeholder = "닉네임을 입력해주세요",
            guideText = "한글, 영문, 숫자만 입력 가능해요 (공백 연속 불가, 최대 12자)",
            showEditIcon = true,
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing32),
        )
    }
}

private class RouteeNicknameTextFieldPreviewParameterProvider :
    PreviewParameterProvider<Pair<String, RouteeTextFieldValidation>> {
    override val values = sequenceOf(
        "" to RouteeTextFieldValidation.None,
        "관악산 날다람쥐" to RouteeTextFieldValidation.Valid,
        "관악산 날다람쥐^&^" to RouteeTextFieldValidation.Invalid,
    )
}

@Preview(widthDp = 360)
@Composable
private fun RouteeNicknameTextFieldPreview(
    @PreviewParameter(RouteeNicknameTextFieldPreviewParameterProvider::class)
    param: Pair<String, RouteeTextFieldValidation>,
) {
    RouteePreviewTheme {
        RouteeNicknameTextField(
            state = rememberTextFieldState(param.first),
            placeholder = "닉네임을 입력해주세요",
            guideText = "한글, 영문, 숫자만 입력 가능해요 (공백 연속 불가, 최대 12자)",
            validation = param.second,
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing32),
        )
    }
}
