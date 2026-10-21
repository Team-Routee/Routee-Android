package com.routee.android.feature.recordedit.main

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.main.model.RecordEditMainSideEffect
import com.routee.android.feature.recordedit.main.model.RecordEditMainUiState
import com.routee.android.feature.recordedit.model.RecapItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class RecordEditMainViewModel @Inject constructor() : ViewModel() {
    val titleEditingState = TextFieldState()

    private val _uiState = MutableStateFlow(RecordEditMainUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<RecordEditMainSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadRecaps()
    }

    fun onPreviousMonthClick() {
        if (!_uiState.value.isPreviousMonthEnabled) return
        moveMonth(_uiState.value.selectedMonth.minusMonths(1))
    }

    fun onNextMonthClick() {
        if (!_uiState.value.isNextMonthEnabled) return
        moveMonth(_uiState.value.selectedMonth.plusMonths(1))
    }

    fun onRetryClick() {
        loadRecaps()
    }

    fun onRecapClick(activityId: Long) {
        completeTitleEdit()
        // TODO API 연동 시 편집 리소스를 먼저 불러오고, 실패하면 이동하지 않고 ShowNetworkErrorToast
        viewModelScope.launch { _sideEffect.send(RecordEditMainSideEffect.NavigateToEditor(activityId)) }
    }

    fun onEditTitleClick(activityId: Long) {
        completeTitleEdit()
        val recap = recapsOrEmpty().find { it.activityId == activityId } ?: return
        titleEditingState.setTextAndPlaceCursorAtEnd(recap.title)
        _uiState.update { it.copy(editingActivityId = activityId) }
    }

    fun onEditTitleComplete(activityId: Long) {
        if (_uiState.value.editingActivityId != activityId) return
        completeTitleEdit()
    }

    private fun completeTitleEdit() {
        val activityId = _uiState.value.editingActivityId ?: return
        val title = titleEditingState.text.toString()
        if (title.isBlank()) {
            _uiState.update { it.copy(editingActivityId = null) }
            return
        }
        _uiState.update { state ->
            state.copy(
                editingActivityId = null,
                recaps = state.recaps.updateTitle(activityId = activityId, title = title),
            )
        }
        // TODO API 연동 시 제목이 바뀌었으면 PATCH /api/v1/activity/{activityId}/title
    }

    private fun moveMonth(month: YearMonth) {
        completeTitleEdit()
        _uiState.update { it.copy(selectedMonth = month) }
        loadRecaps()
    }

    // TODO API 연동 시 회원 요약(GET /summary)의 joinDate로 joinedMonth를 채운다. 가입 월 이전으로 이동 막기 (RT-EDIT-001)
    // TODO API 연동 시 selectedMonth로 GET /api/v1/activity/recap?year=&month= 결과를 채운다 (실패 시 UiState.Failure)
    private fun loadRecaps() {
        _uiState.update { it.copy(recaps = UiState.Empty) }
    }

    private fun recapsOrEmpty(): List<RecapItemUiModel> =
        (_uiState.value.recaps as? UiState.Success)?.data.orEmpty()

    private fun UiState<ImmutableList<RecapItemUiModel>>.updateTitle(
        activityId: Long,
        title: String,
    ): UiState<ImmutableList<RecapItemUiModel>> {
        if (this !is UiState.Success) return this
        return UiState.Success(
            data.map { recap -> if (recap.activityId == activityId) recap.copy(title = title) else recap }
                .toImmutableList(),
        )
    }
}
