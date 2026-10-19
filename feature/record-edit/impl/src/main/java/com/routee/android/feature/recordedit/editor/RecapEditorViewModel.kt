package com.routee.android.feature.recordedit.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.navigation.util.NavKeyViewModelFactory
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.api.RecapEditorNavKey
import com.routee.android.feature.recordedit.editor.model.RecapEditorContent
import com.routee.android.feature.recordedit.editor.model.RecapEditorSideEffect
import com.routee.android.feature.recordedit.editor.model.RecapEditorUiState
import com.routee.android.feature.recordedit.editor.model.RecapSticker
import com.routee.android.feature.recordedit.editor.model.StickerFrame
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.RecapEditTab
import com.routee.android.feature.recordedit.model.StickerType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
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

            RecapEditTab.BRIGHTNESS, RecapEditTab.COLOR, RecapEditTab.STICKER -> _uiState.update {
                // 스티커 탭을 열거나 닫으면 선택을 푼다. 서브탭은 처음에 미선택 (RT-EDIT-007)
                val isStickerTabToggled = tab == RecapEditTab.STICKER || it.selectedTab == RecapEditTab.STICKER
                it.copy(
                    selectedTab = if (it.selectedTab == tab) null else tab,
                    selectedSticker = if (isStickerTabToggled) null else it.selectedSticker,
                )
            }
        }
    }

    fun onPreviewClick() {
        _uiState.update { it.copy(selectedTab = null, selectedSticker = null) }
    }

    fun onBrightnessChange(brightness: Float) {
        _uiState.update { it.copy(brightness = brightness) }
    }

    fun onColorClick(recapColor: RecapColor) {
        _uiState.update { it.copy(recapColor = recapColor) }
    }

    fun onStickerItemClick(type: StickerType) {
        val state = _uiState.value
        if (type == StickerType.ROUTE && state.routeNames().isEmpty()) {
            viewModelScope.launch { _sideEffect.send(RecapEditorSideEffect.ShowNoRouteToast) }
            return
        }
        if (state.stickers.any { it.type == type }) {
            onStickerClick(type)
        } else {
            // 삭제 후 다시 추가하거나 루트를 처음 추가하면 가운데 (RT-EDIT-007)
            _uiState.update {
                it.copy(
                    stickers = (it.stickers + RecapSticker(type = type, isCentered = true)).toImmutableList(),
                    selectedSticker = type,
                )
            }
        }
    }

    fun onStickerClick(type: StickerType) {
        _uiState.update { state ->
            val sticker = state.stickers.find { it.type == type } ?: return@update state
            state.copy(
                stickers = (state.stickers - sticker + sticker).toImmutableList(),
                selectedSticker = type,
            )
        }
    }

    fun onStickerFrameChange(
        type: StickerType,
        frame: StickerFrame,
    ) {
        _uiState.update { state ->
            state.copy(
                stickers = state.stickers
                    .map { if (it.type == type) it.copy(frame = frame) else it }
                    .toImmutableList(),
            )
        }
    }

    fun onStickerDeleteClick(type: StickerType) {
        _uiState.update { state ->
            state.copy(
                stickers = state.stickers.filterNot { it.type == type }.toImmutableList(),
                selectedSticker = null,
            )
        }
    }

    fun onResetClick() {
        if (!_uiState.value.hasChanges) return
        _uiState.update { it.copy(isResetDialogVisible = true) }
    }

    fun onResetConfirm() {
        // 첫 화면으로 되돌린다 (RT-EDIT-004)
        _uiState.update { RecapEditorUiState(content = it.content) }
    }

    fun onResetDismiss() {
        _uiState.update { it.copy(isResetDialogVisible = false) }
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

    private fun RecapEditorUiState.routeNames(): List<String> =
        (content as? UiState.Success)?.data?.routeNames.orEmpty()

    private fun navigateBack() {
        viewModelScope.launch { _sideEffect.send(RecapEditorSideEffect.NavigateBack) }
    }

    @AssistedFactory
    interface Factory : NavKeyViewModelFactory<RecapEditorNavKey, RecapEditorViewModel>
}
