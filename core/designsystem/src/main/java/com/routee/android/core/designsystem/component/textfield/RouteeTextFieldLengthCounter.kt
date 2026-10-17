package com.routee.android.core.designsystem.component.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.routee.android.core.common.extension.graphemeLength
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 입력창 글자 수 (예: 3/16)
 *
 * 이모지 같은 결합 문자도 한 글자로 센다
 */
@Composable
fun RouteeTextFieldLengthCounter(
    state: TextFieldState,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.designsystem_text_field_length_counter, state.text.graphemeLength(), maxLength),
        style = RouteeTheme.typography.labelM14,
        color = RouteeTheme.colors.white30,
        modifier = modifier,
    )
}
