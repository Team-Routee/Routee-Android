package com.routee.android.core.designsystem.component.textfield

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 입력값 검증 결과. 검증은 호출하는 쪽(ViewModel)에서 하고 결과만 넘긴다
 */
enum class RouteeTextFieldValidation {
    None,
    Valid,
    Invalid,
}

/**
 * 테두리 입력창의 화면 상태. 포커스 여부와 [RouteeTextFieldValidation]으로 정해진다
 */
internal enum class RouteeTextFieldStatus {
    Initial,
    Focus,
    Typing,
    Success,
    Error,
    ;

    val borderColor: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            Error -> RouteeTheme.colors.statusError
            Focus, Typing -> RouteeTheme.colors.statusInfo
            Initial, Success -> RouteeTheme.colors.white30
        }

    val feedbackColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (this == Error) RouteeTheme.colors.statusError else RouteeTheme.colors.white60

    @get:DrawableRes
    val trailingIconRes: Int?
        get() = when (this) {
            Error -> R.drawable.ic_error
            Typing, Success -> R.drawable.ic_success_green
            Initial, Focus -> null
        }

    companion object {
        fun from(
            isFocused: Boolean,
            validation: RouteeTextFieldValidation,
        ): RouteeTextFieldStatus = when {
            validation == RouteeTextFieldValidation.Invalid -> Error
            isFocused && validation == RouteeTextFieldValidation.Valid -> Typing
            isFocused -> Focus
            validation == RouteeTextFieldValidation.Valid -> Success
            else -> Initial
        }
    }
}
