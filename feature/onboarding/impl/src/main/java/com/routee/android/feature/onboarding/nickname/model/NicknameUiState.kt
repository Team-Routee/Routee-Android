package com.routee.android.feature.onboarding.nickname.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation

@Immutable
internal data class NicknameUiState(
    val validation: RouteeTextFieldValidation = RouteeTextFieldValidation.None,
    // 가입 요청 중, 로딩으로 입력을 막고 시작하기와 뒤로가기를 막음
    val isRegistering: Boolean = false,
) {
    val isStartEnabled: Boolean get() = validation == RouteeTextFieldValidation.Valid && !isRegistering
}
