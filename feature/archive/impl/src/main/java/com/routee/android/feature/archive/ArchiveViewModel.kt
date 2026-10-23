package com.routee.android.feature.archive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.archive.repository.ArchiveRepository
import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.component.RecordSheetItem
import com.routee.android.feature.archive.model.ArchiveSideEffect
import com.routee.android.feature.archive.model.ArchiveUiState
import com.routee.android.feature.archive.model.archiveContent
import com.routee.android.feature.archive.model.toArchiveProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class ArchiveViewModel @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val memberRepository: MemberRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ArchiveUiState().let { initial ->
            val month = savedStateHandle.get<String>("selectedMonth")?.let(YearMonth::parse)
            initial.copy(selectedMonth = month?.coerceAtMost(initial.currentMonth) ?: initial.currentMonth)
        },
    )
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = Channel<ArchiveSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()
    private var member: MemberSummary? = null
    private var loadJob: Job? = null
    private var recordsJob: Job? = null

    init {
        loadSummary()
    }

    fun onPreviousMonthClick() {
        if (_uiState.value.isPreviousMonthEnabled) moveMonth(_uiState.value.selectedMonth.minusMonths(1))
    }

    fun onNextMonthClick() {
        if (_uiState.value.isNextMonthEnabled) moveMonth(_uiState.value.selectedMonth.plusMonths(1))
    }

    fun onRetryClick() = loadSummary()

    fun onRecordChanged() {
        member = null
        loadSummary()
    }

    fun onDateClick(date: LocalDate) = loadRecords(date)

    fun onRecordClick(activityId: Long) {
        val date = _uiState.value.selectedDate ?: return
        if (!_uiState.value.isLoadingRecords) loadRecords(date, activityId)
    }

    fun onRecordSheetDismiss() {
        recordsJob?.cancel()
        _uiState.update { it.copy(selectedDate = null, records = persistentListOf(), isLoadingRecords = false) }
    }

    private fun loadRecords(date: LocalDate, activityId: Long? = null) {
        recordsJob?.cancel()
        _uiState.update {
            it.copy(
                selectedDate = date,
                records = if (it.selectedDate == date) it.records else persistentListOf(),
                isLoadingRecords = true,
            )
        }
        recordsJob = viewModelScope.launch {
            val result = archiveRepository.getDailyRecords(date)
            currentCoroutineContext().ensureActive()
            result
                .onSuccess { records ->
                    _uiState.update { it.copy(isLoadingRecords = false, isOffline = false) }
                    if (records.isEmpty()) {
                        loadSummary()
                        return@onSuccess
                    }
                    val selected = if (activityId == null) {
                        records.singleOrNull()
                    } else {
                        records.find { it.activityId == activityId }
                    }
                    if (selected != null) {
                        _uiState.update { it.copy(selectedDate = null, records = persistentListOf()) }
                        _sideEffect.send(ArchiveSideEffect.NavigateToDetail(selected.activityId))
                    } else {
                        _uiState.update {
                            it.copy(
                                records = records.map { record ->
                                    RecordSheetItem(record.activityId, record.title, record.thumbnailUrl)
                                }.toImmutableList(),
                            )
                        }
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    val offline = error is ApiException.NetworkConnection
                    _uiState.update { it.copy(isLoadingRecords = false, isOffline = offline) }
                    _sideEffect.send(
                        if (offline) ArchiveSideEffect.NetworkUnavailable else ArchiveSideEffect.RecordLoadFailed,
                    )
                }
        }
    }

    private fun moveMonth(month: YearMonth) {
        savedStateHandle["selectedMonth"] = month.toString()
        _uiState.update { it.copy(selectedMonth = month) }
        loadSummary()
    }

    private fun loadSummary() {
        onRecordSheetDismiss()
        loadJob?.cancel()
        val month = _uiState.value.selectedMonth
        _uiState.update { it.copy(isRefreshing = true) }
        loadJob = viewModelScope.launch {
            suspendRunCatching {
                coroutineScope {
                    val profile = async { member ?: memberRepository.getMemberSummary().getOrThrow() }
                    val summaries = async { archiveRepository.getMonthlySummary(month).getOrThrow() }
                    val loadedMember = profile.await().also { member = it }
                    archiveContent(loadedMember.toArchiveProfile(), month, summaries.await())
                }
            }.onSuccess { content ->
                _uiState.update { it.copy(content = UiState.Success(content), isRefreshing = false, isOffline = false) }
            }.onFailure { error ->
                Timber.e(error)
                val isOffline = error is ApiException.NetworkConnection
                _uiState.update {
                    it.copy(
                        content = when {
                            !isOffline || it.content !is UiState.Success -> UiState.Failure(error)
                            it.content.data.month == month -> it.content
                            else -> UiState.Success(archiveContent(it.content.data.profile, month, emptyList()))
                        },
                        isRefreshing = false,
                        isOffline = isOffline,
                    )
                }
                if (isOffline) _sideEffect.send(ArchiveSideEffect.NetworkUnavailable)
            }
        }
    }
}
