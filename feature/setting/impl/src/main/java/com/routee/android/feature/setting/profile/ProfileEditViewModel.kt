package com.routee.android.feature.setting.profile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation
import com.routee.android.core.domain.member.NicknameValidation
import com.routee.android.core.domain.member.ValidateNicknameUseCase
import com.routee.android.feature.setting.profile.model.ProfileEditSideEffect
import com.routee.android.feature.setting.profile.model.ProfileEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// TODO 프로필 조회 API (조회 중 전역 로딩)
private const val SAMPLE_NICKNAME = "관악산 날다람쥐"

@HiltViewModel
internal class ProfileEditViewModel @Inject constructor(
    private val validateNickname: ValidateNicknameUseCase,
) : ViewModel() {
    val nicknameState = TextFieldState(SAMPLE_NICKNAME)

    private val _uiState = MutableStateFlow(
        ProfileEditUiState(savedNickname = SAMPLE_NICKNAME, nickname = SAMPLE_NICKNAME),
    )
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ProfileEditSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        observeNickname()
    }

    fun onNicknameFocusChange(hasFocus: Boolean) {
        if (hasFocus) return
        val text = nicknameState.text
        val trimmed = text.trimEnd()
        if (trimmed.length != text.length) nicknameState.setTextAndPlaceCursorAtEnd(trimmed.toString())
    }

    fun onProfileImageClick() {
        // TODO 사진이 없으면 갤러리 바로 열기
        if (_uiState.value.profileImageUrl == null) return
        _uiState.update { it.copy(isPhotoDialogVisible = true) }
    }

    fun onPhotoDialogDismiss() {
        _uiState.update { it.copy(isPhotoDialogVisible = false) }
    }

    fun onAlbumClick() {
        // TODO 갤러리 열기
        onPhotoDialogDismiss()
    }

    fun onDefaultImageClick() {
        _uiState.update { it.copy(profileImageUrl = null, isPhotoDialogVisible = false) }
    }

    fun onSaveClick() {
        onNicknameChange(nicknameState.text)
        if (!_uiState.value.isSaveEnabled) return
        // TODO 프로필 변경 API
        _uiState.update {
            it.copy(
                savedNickname = it.nickname,
                savedProfileImageUrl = it.profileImageUrl,
                validation = RouteeTextFieldValidation.None,
            )
        }
        viewModelScope.launch { _sideEffect.send(ProfileEditSideEffect.ShowSavedToast) }
    }

    private fun observeNickname() {
        viewModelScope.launch {
            snapshotFlow { nicknameState.text.toString() to nicknameState.composition }
                .filter { (_, composition) -> composition == null }
                .collect { (text, _) -> onNicknameChange(text) }
        }
    }

    private fun onNicknameChange(text: CharSequence) {
        val nickname = text.trimEnd().toString()
        _uiState.update {
            it.copy(nickname = nickname, validation = nicknameValidation(nickname, it.savedNickname))
        }
    }

    private fun nicknameValidation(nickname: String, savedNickname: String): RouteeTextFieldValidation {
        if (nickname == savedNickname) return RouteeTextFieldValidation.None
        return when (validateNickname(nickname)) {
            NicknameValidation.EMPTY -> RouteeTextFieldValidation.None
            NicknameValidation.VALID -> RouteeTextFieldValidation.Valid
            NicknameValidation.INVALID -> RouteeTextFieldValidation.Invalid
        }
    }
}
