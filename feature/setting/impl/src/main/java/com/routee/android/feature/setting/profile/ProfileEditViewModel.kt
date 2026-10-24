package com.routee.android.feature.setting.profile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.member.exception.MemberException
import com.routee.android.core.data.member.model.MemberProfile
import com.routee.android.core.data.member.repository.ProfileRepository
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
import timber.log.Timber

@HiltViewModel
internal class ProfileEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val validateNickname: ValidateNicknameUseCase,
) : ViewModel() {
    val nicknameState = TextFieldState()

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ProfileEditSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadProfile()
        observeNickname()
    }

    fun onRetryClick() {
        loadProfile()
    }

    fun onNicknameFocusChange(hasFocus: Boolean) {
        if (hasFocus) return
        val text = nicknameState.text
        val trimmed = text.trimEnd()
        if (trimmed.length != text.length) nicknameState.setTextAndPlaceCursorAtEnd(trimmed.toString())
    }

    fun onProfileImageClick() {
        if (_uiState.value.profileImageUrl == null) {
            openGallery()
            return
        }
        _uiState.update { it.copy(isPhotoDialogVisible = true) }
    }

    fun onPhotoDialogDismiss() {
        _uiState.update { it.copy(isPhotoDialogVisible = false) }
    }

    fun onAlbumClick() {
        onPhotoDialogDismiss()
        openGallery()
    }

    fun onDefaultImageClick() {
        _uiState.update { it.copy(profileImageUrl = null, isPhotoDialogVisible = false) }
    }

    fun onProfileImageCropComplete(imageUri: String) {
        _uiState.update { it.copy(profileImageUrl = imageUri) }
    }

    fun onProfileImageCropCancel() {
        openGallery()
    }

    fun onSaveClick() {
        onNicknameChange(nicknameState.text)
        val state = _uiState.value
        if (state.isLoading || !state.isSaveEnabled) return
        saveProfile(state)
    }

    private fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, isLoadFailed = false) }
        viewModelScope.launch {
            profileRepository.getProfile()
                .onSuccess(::applyProfile)
                .onFailure { e ->
                    Timber.e(e)
                    _uiState.update { it.copy(isLoadFailed = true) }
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun applyProfile(profile: MemberProfile) {
        _uiState.update {
            it.copy(
                savedNickname = profile.nickname,
                nickname = profile.nickname,
                savedProfileImageUrl = profile.profileImageUrl,
                profileImageUrl = profile.profileImageUrl,
                validation = RouteeTextFieldValidation.None,
            )
        }
        nicknameState.setTextAndPlaceCursorAtEnd(profile.nickname)
    }

    private fun saveProfile(state: ProfileEditUiState) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            saveChanges(state)
                .onSuccess {
                    _uiState.update { it.copy(validation = RouteeTextFieldValidation.None) }
                    _sideEffect.send(ProfileEditSideEffect.ShowSavedToast)
                }
                .onFailure { e ->
                    // TODO P1 기획: 저장 실패 안내 문구 (iOS는 안내 없음)
                    if (e is MemberException.NetworkUnavailable) {
                        _sideEffect.send(ProfileEditSideEffect.ShowNetworkErrorToast)
                    } else {
                        Timber.e(e)
                        _sideEffect.send(ProfileEditSideEffect.ShowUnknownErrorToast)
                    }
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun saveChanges(state: ProfileEditUiState): Result<Unit> {
        saveNickname(state).onFailure { return Result.failure(it) }
        return saveProfileImage(state)
    }

    private suspend fun saveNickname(state: ProfileEditUiState): Result<Unit> {
        if (!state.isNicknameChanged) return Result.success(Unit)
        return profileRepository.updateNickname(state.nickname)
            .map { nickname -> _uiState.update { it.copy(savedNickname = nickname) } }
    }

    private suspend fun saveProfileImage(state: ProfileEditUiState): Result<Unit> {
        val imageUri = state.profileImageUrl
        val result = when {
            !state.isProfileImageChanged -> return Result.success(Unit)
            imageUri == null -> profileRepository.resetProfileImage().map { null }
            else -> profileRepository.updateProfileImage(imageUri)
        }
        return result.map { url -> _uiState.update { it.copy(savedProfileImageUrl = url, profileImageUrl = url) } }
    }

    private fun openGallery() {
        viewModelScope.launch { _sideEffect.send(ProfileEditSideEffect.OpenGallery) }
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
