package com.routee.android.feature.recordedit.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.api.RecapEditorNavKey
import com.routee.android.feature.recordedit.editor.model.RecapEditorContent
import com.routee.android.feature.recordedit.editor.model.RecapEditorSideEffect
import com.routee.android.feature.recordedit.editor.model.RecapEditorUiState
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.RecapEditTab
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

@HiltViewModel(assistedFactory = RecapEditorViewModel.Factory::class)
internal class RecapEditorViewModel @AssistedInject constructor(
    @Assisted private val key: RecapEditorNavKey,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecapEditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<RecapEditorSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadContent()
    }

    fun onRetryClick() {
        loadContent()
    }

    fun onTabClick(tab: RecapEditTab) {
        when (tab) {
            // TODO #65 갤러리 사진 선택으로 이동
            RecapEditTab.BACKGROUND -> Unit

            // TODO #64 스티커 서브탭 노출
            RecapEditTab.STICKER -> Unit

            RecapEditTab.BRIGHTNESS, RecapEditTab.COLOR -> _uiState.update {
                it.copy(selectedTab = if (it.selectedTab == tab) null else tab)
            }
        }
    }

    fun onPreviewClick() {
        _uiState.update { it.copy(selectedTab = null) }
    }

    fun onBrightnessChange(brightness: Float) {
        _uiState.update { it.copy(brightness = brightness, hasChanges = true) }
    }

    fun onColorClick(recapColor: RecapColor) {
        if (_uiState.value.recapColor == recapColor) return
        _uiState.update { it.copy(recapColor = recapColor, hasChanges = true) }
    }

    fun onBackClick() {
        if (_uiState.value.hasChanges) {
            _uiState.update { it.copy(isEditStopDialogVisible = true) }
        } else {
            navigateBack()
        }
    }

    fun onEditStopConfirm() {
        _uiState.update { it.copy(isEditStopDialogVisible = false) }
        navigateBack()
    }

    fun onEditStopDismiss() {
        _uiState.update { it.copy(isEditStopDialogVisible = false) }
    }

    // TODO #66 완료 애니메이션, 결과 화면
    fun onCompleteClick() = Unit

    // TODO API 연동 시 key.activityId로 GET /recap, /track 결과를 채운다 (실패 시 UiState.Failure)
    private fun loadContent() {
        _uiState.update { it.copy(content = UiState.Success(RecapEditorContent.EMPTY)) }
    }

    private fun navigateBack() {
        viewModelScope.launch { _sideEffect.send(RecapEditorSideEffect.NavigateBack) }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<RecapEditorNavKey, RecapEditorViewModel>
}
