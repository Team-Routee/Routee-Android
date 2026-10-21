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
        val originalTitle = recapsOrEmpty().find { it.activityId == activityId }?.title
        _uiState.update { it.copy(editingActivityId = null) }
        if (title.isBlank() || originalTitle == null || title == originalTitle) return

        updateTitle(activityId = activityId, title = title)
        viewModelScope.launch {
            recapRepository.updateRecapTitle(activityId = activityId, title = title)
                .onFailure { e ->
                    Timber.e(e)
                    restoreTitle(activityId = activityId, failedTitle = title, originalTitle = originalTitle)
                }
        }
    }

    private fun updateTitle(
        activityId: Long,
        title: String,
    ) {
        _uiState.update { it.copy(recaps = it.recaps.updateTitle(activityId = activityId, title = title)) }
    }

    private fun restoreTitle(
        activityId: Long,
        failedTitle: String,
        originalTitle: String,
    ) {
        val currentTitle = recapsOrEmpty().find { it.activityId == activityId }?.title
        if (currentTitle == failedTitle) updateTitle(activityId = activityId, title = originalTitle)
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
            val joinedMonth = async { getJoinedMonth() }
            val recaps = recapRepository.getMonthlyRecaps(month)
            joinedMonth.await()
                .onSuccess { joined -> _uiState.update { it.copy(joinedMonth = joined) } }
                .fold(onSuccess = { recaps }, onFailure = { Result.failure(it) })
                .onSuccess { result -> _uiState.update { it.copy(recaps = result.toRecapsUiState()) } }
                .onFailure { e ->
                    Timber.e(e)
                    _uiState.update { it.copy(recaps = UiState.Failure(e)) }
                }
        }
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
