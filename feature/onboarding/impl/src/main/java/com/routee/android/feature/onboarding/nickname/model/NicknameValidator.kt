package com.routee.android.feature.onboarding.nickname.model

import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation

internal object NicknameValidator {
    private const val MAX_LENGTH = 12
    private val NicknamePattern = Regex("^[가-힣a-zA-Z0-9]+( [가-힣a-zA-Z0-9]+)*$")

    fun validate(nickname: CharSequence): RouteeTextFieldValidation {
        val trimmed = nickname.trimEnd()
        return when {
            trimmed.isEmpty() -> RouteeTextFieldValidation.None
            trimmed.length <= MAX_LENGTH && NicknamePattern.matches(trimmed) -> RouteeTextFieldValidation.Valid
            else -> RouteeTextFieldValidation.Invalid
        }
    }
}
