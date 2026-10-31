package com.routee.android.feature.onboarding.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.auth.exception.AuthException
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.core.data.connectivity.repository.ConnectivityRepository
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.onboarding.login.model.LoginSideEffect
import com.routee.android.feature.onboarding.login.model.LoginUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = LoginViewModel.Factory::class)
internal class LoginViewModel @AssistedInject constructor(
    @Assisted key: LoginNavKey,
    private val authRepository: AuthRepository,
    private val connectivityRepository: ConnectivityRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState(isSessionExpiredDialogVisible = key.isSessionExpired))
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<LoginSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var isLoggingIn = false

    fun isConnected(): Boolean = connectivityRepository.isConnected()

    fun onIdTokenResult(result: Result<String>) {
        val idToken = result.getOrElse { e ->
            onGoogleLoginFailure(e)
            return
        }
        if (isLoggingIn) return
        isLoggingIn = true
        viewModelScope.launch {
            login(idToken)
            isLoggingIn = false
        }
    }

    fun onSessionExpiredDialogDismiss() {
        _uiState.update { it.copy(isSessionExpiredDialogVisible = false) }
    }

    private fun onGoogleLoginFailure(throwable: Throwable) {
        val sideEffect = when (throwable) {
            is GoogleLoginException.Cancelled -> return

            is GoogleLoginException.NetworkUnavailable -> LoginSideEffect.ShowNetworkErrorToast

            else -> {
                Timber.e(throwable)
                LoginSideEffect.ShowUnknownErrorToast
            }
        }
        viewModelScope.launch { _sideEffect.send(sideEffect) }
    }

    private suspend fun login(idToken: String) {
        authRepository.login(idToken, SocialProvider.GOOGLE)
            .onSuccess { result ->
                val sideEffect = when (result) {
                    LoginResult.Success -> LoginSideEffect.NavigateToHome
                    is LoginResult.NeedRegister -> LoginSideEffect.NavigateToTerms(result.idToken)
                }
                _sideEffect.send(sideEffect)
            }
            .onFailure { e ->
                if (e is AuthException.NetworkUnavailable) {
                    _sideEffect.send(LoginSideEffect.ShowNetworkErrorToast)
                } else {
                    Timber.e(e)
                    _sideEffect.send(LoginSideEffect.ShowUnknownErrorToast)
                }
            }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<LoginNavKey, LoginViewModel>
}
