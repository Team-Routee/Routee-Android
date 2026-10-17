package com.routee.android.feature.onboarding.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.feature.onboarding.login.model.LoginSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _sideEffect = Channel<LoginSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var isLoggingIn = false

    fun onIdTokenResult(result: Result<String>) {
        val idToken = result
            .onFailure { e -> if (e !is GoogleLoginException.Cancelled) Timber.e(e) }
            .getOrNull() ?: return
        if (isLoggingIn) return
        isLoggingIn = true
        viewModelScope.launch {
            login(idToken)
            isLoggingIn = false
        }
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
            .onFailure { e -> Timber.e(e) }
    }
}
