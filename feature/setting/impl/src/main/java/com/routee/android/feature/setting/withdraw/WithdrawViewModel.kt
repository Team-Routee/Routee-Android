package com.routee.android.feature.setting.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
internal class WithdrawViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState: StateFlow<WithdrawUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<WithdrawSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAgreeChange(agreed: Boolean) {
        _uiState.update { it.copy(isAgreed = agreed) }
    }

    fun onWithdrawClick() {
        if (!_uiState.value.isAgreed) return
        // TODO 회원 탈퇴 API(DELETE /api/v1/member/withdraw) 연결, 실패 처리
        viewModelScope.launch { _sideEffect.send(WithdrawSideEffect.NavigateToLogin) }
    }
}
