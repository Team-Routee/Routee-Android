package com.routee.android.feature.recordedit.main

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.main.model.RecordEditMainSideEffect
import com.routee.android.feature.recordedit.main.model.RecordEditMainUiState
import com.routee.android.feature.recordedit.model.RecapItemUiModel
import com.routee.android.feature.recordedit.model.toRecapItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class RecordEditMainViewModel @Inject constructor(
    private val recapRepository: RecapRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    val titleEditingState = TextFieldState()

    private val _uiState = MutableStateFlow(RecordEditMainUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<RecordEditMainSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var loadJob: Job? = null

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

    private fun loadRecaps() {
        loadJob?.cancel()
        val month = _uiState.value.selectedMonth
        _uiState.update { it.copy(recaps = UiState.Loading) }
        loadJob = viewModelScope.launch {
            val joinedMonthResult = async { getJoinedMonth() }
            val recapsResult = recapRepository.getMonthlyRecaps(month)

            val joinedMonth = joinedMonthResult.await().getOrElse { return@launch onLoadRecapsFailure(it) }
            _uiState.update { it.copy(joinedMonth = joinedMonth) }

            val recaps = recapsResult.getOrElse { return@launch onLoadRecapsFailure(it) }
            _uiState.update { it.copy(recaps = recaps.toRecapsUiState()) }
        }
    }

    private fun onLoadRecapsFailure(throwable: Throwable) {
        Timber.e(throwable)
        _uiState.update { it.copy(recaps = UiState.Failure(throwable)) }
    }

    private suspend fun getJoinedMonth(): Result<YearMonth> {
        _uiState.value.joinedMonth?.let { return Result.success(it) }
        return memberRepository.getMemberSummary().map { YearMonth.from(it.joinDate) }
    }

    private fun List<ActivityRecap>.toRecapsUiState(): UiState<ImmutableList<RecapItemUiModel>> =
        if (isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(sortedByDescending { it.activityDate }.map { it.toRecapItemUiModel() }.toImmutableList())
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
