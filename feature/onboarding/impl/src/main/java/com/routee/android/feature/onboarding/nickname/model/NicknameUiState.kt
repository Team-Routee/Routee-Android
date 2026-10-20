package com.routee.android.feature.onboarding.nickname.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation

@Immutable
internal data class NicknameUiState(
    val validation: RouteeTextFieldValidation = RouteeTextFieldValidation.None,
) {
    val isStartEnabled: Boolean get() = validation == RouteeTextFieldValidation.Valid
}
