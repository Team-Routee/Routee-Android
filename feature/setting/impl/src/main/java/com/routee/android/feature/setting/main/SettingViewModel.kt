package com.routee.android.feature.setting.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.feature.setting.main.model.SettingSideEffect
import com.routee.android.feature.setting.main.model.SettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<SettingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var isLoggingOut = false

    fun onLogoutClick() {
        _uiState.update { it.copy(isLogoutDialogVisible = true) }
    }

    fun onLogoutDismiss() {
        _uiState.update { it.copy(isLogoutDialogVisible = false) }
    }

    fun onLogoutConfirm() {
        _uiState.update { it.copy(isLogoutDialogVisible = false) }
        if (isLoggingOut) return
        isLoggingOut = true
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess { _sideEffect.send(SettingSideEffect.NavigateToLogin) }
                .onFailure { e ->
                    Timber.e(e)
                    _sideEffect.send(SettingSideEffect.ShowUnknownErrorToast)
                }
            isLoggingOut = false
        }
    }
}
