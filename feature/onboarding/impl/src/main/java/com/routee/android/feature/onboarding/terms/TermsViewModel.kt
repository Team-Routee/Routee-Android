package com.routee.android.feature.onboarding.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.feature.onboarding.api.TermsNavKey
import com.routee.android.feature.onboarding.login.GoogleLoginManager
import com.routee.android.feature.onboarding.terms.model.TermsItem
import com.routee.android.feature.onboarding.terms.model.TermsSideEffect
import com.routee.android.feature.onboarding.terms.model.TermsUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = TermsViewModel.Factory::class)
internal class TermsViewModel @AssistedInject constructor(
    @Assisted private val key: TermsNavKey,
    private val googleLoginManager: GoogleLoginManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<TermsSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var isNavigatingBack = false

    fun onAllCheckedChange(checked: Boolean) {
        _uiState.update { it.copy(checkedItems = if (checked) TermsItem.entries.toSet() else emptySet()) }
    }

    fun onItemCheckedChange(
        item: TermsItem,
        checked: Boolean,
    ) {
        _uiState.update { state ->
            state.copy(checkedItems = if (checked) state.checkedItems + item else state.checkedItems - item)
        }
    }

    fun onNextClick() {
        val state = _uiState.value
        if (!state.isNextEnabled) return
        viewModelScope.launch {
            _sideEffect.send(
                TermsSideEffect.NavigateToNickname(
                    idToken = key.idToken,
                    marketingConsent = state.isChecked(TermsItem.MARKETING),
                ),
            )
        }
    }

    fun onBackClick() {
        if (isNavigatingBack) return
        isNavigatingBack = true
        viewModelScope.launch(NonCancellable) {
            googleLoginManager.clearCredential().onFailure { e -> Timber.w(e) }
        }
        viewModelScope.launch { _sideEffect.send(TermsSideEffect.NavigateBack) }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<TermsNavKey, TermsViewModel>
}
