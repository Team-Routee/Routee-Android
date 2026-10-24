package com.routee.android.feature.setting.profile.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation

@Immutable
internal data class ProfileEditUiState(
    val isLoading: Boolean = true,
    val isLoadFailed: Boolean = false,
    val savedNickname: String = "",
    val nickname: String = "",
    val savedProfileImageUrl: String? = null,
    val profileImageUrl: String? = null,
    val validation: RouteeTextFieldValidation = RouteeTextFieldValidation.None,
    val isPhotoDialogVisible: Boolean = false,
) {
    val isNicknameChanged: Boolean get() = nickname != savedNickname
    val isProfileImageChanged: Boolean get() = profileImageUrl != savedProfileImageUrl

    val isSaveEnabled: Boolean
        get() = when {
            isNicknameChanged -> validation == RouteeTextFieldValidation.Valid
            else -> isProfileImageChanged
        }
}
