package com.routee.android.feature.setting.profile.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation

@Immutable
internal data class ProfileEditUiState(
    val savedNickname: String = "",
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val validation: RouteeTextFieldValidation = RouteeTextFieldValidation.None,
) {
    val isSaveEnabled: Boolean
        get() = validation == RouteeTextFieldValidation.Valid && nickname != savedNickname
}
