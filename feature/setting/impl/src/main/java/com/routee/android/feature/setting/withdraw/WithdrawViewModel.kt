package com.routee.android.feature.setting.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.auth.exception.AuthException
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.core.domain.auth.ClearDeviceDataUseCase
import com.routee.android.feature.setting.withdraw.model.WithdrawSideEffect
import com.routee.android.feature.setting.withdraw.model.WithdrawUiState
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
internal class WithdrawViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val clearDeviceData: ClearDeviceDataUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState: StateFlow<WithdrawUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WithdrawSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var isWithdrawing = false

    fun onAgreeChange(agreed: Boolean) {
        _uiState.update { it.copy(isAgreed = agreed) }
    }

    fun onWithdrawClick() {
        if (!_uiState.value.isAgreed || isWithdrawing) return
        isWithdrawing = true
        viewModelScope.launch {
            authRepository.withdraw()
                .onSuccess {
                    clearDeviceData().onFailure { Timber.e(it, "기기 데이터 삭제 실패") }
                    _sideEffect.send(WithdrawSideEffect.NavigateToLogin)
                }
                .onFailure { e ->
                    if (e is AuthException.NetworkUnavailable) {
                        _sideEffect.send(WithdrawSideEffect.ShowNetworkErrorToast)
                    } else {
                        Timber.e(e)
                        _sideEffect.send(WithdrawSideEffect.ShowUnknownErrorToast)
                    }
                }
            isWithdrawing = false
        }
    }
}
