package com.routee.android.feature.onboarding.nickname

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.onboarding.api.NicknameNavKey
import com.routee.android.feature.onboarding.nickname.model.NicknameSideEffect
import com.routee.android.feature.onboarding.nickname.model.NicknameUiState
import com.routee.android.feature.onboarding.nickname.model.NicknameValidator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = NicknameViewModel.Factory::class)
internal class NicknameViewModel @AssistedInject constructor(
    @Assisted private val key: NicknameNavKey,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val nicknameState = TextFieldState()

    private val _uiState = MutableStateFlow(NicknameUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<NicknameSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var isRegistering = false

    init {
        viewModelScope.launch {
            snapshotFlow { nicknameState.text.toString() to nicknameState.composition }
                .filter { (_, composition) -> composition == null }
                .collect { (text, _) ->
                    _uiState.update { it.copy(validation = NicknameValidator.validate(text)) }
                }
        }
    }

    fun onFocusChange(hasFocus: Boolean) {
        if (hasFocus) return
        val text = nicknameState.text
        val trimmed = text.trimEnd()
        if (trimmed.length != text.length) nicknameState.setTextAndPlaceCursorAtEnd(trimmed.toString())
    }

    fun onStartClick() {
        if (!_uiState.value.isStartEnabled || isRegistering) return
        isRegistering = true
        viewModelScope.launch {
            authRepository.register(
                nickname = nicknameState.text.trim().toString(),
                idToken = key.idToken,
                provider = SocialProvider.GOOGLE,
                agreements = Agreements(
                    serviceTerms = true,
                    privacyPolicy = true,
                    locationServiceTerms = true,
                    over14 = true,
                    marketingConsent = key.marketingConsent,
                ),
            )
                .onSuccess { _sideEffect.send(NicknameSideEffect.NavigateToHome) }
                .onFailure { e -> Timber.e(e) }
            isRegistering = false
        }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<NicknameNavKey, NicknameViewModel>
}
