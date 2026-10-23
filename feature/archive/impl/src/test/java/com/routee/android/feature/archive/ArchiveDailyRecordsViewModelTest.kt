package com.routee.android.feature.archive

import androidx.lifecycle.SavedStateHandle
import com.routee.android.core.data.archive.model.ArchiveRecord
import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.core.data.archive.repository.ArchiveRepository
import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.model.ArchiveSideEffect
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveDailyRecordsViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()

    @Test
    fun singleRecordNavigatesWhileMultipleRecordsKeepServerOrder() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        val date = LocalDate.now()
        repository.records = listOf(ArchiveRecord(4, "산행", null))

        viewModel.onDateClick(date)
        assertEquals(ArchiveSideEffect.NavigateToDetail(4), viewModel.sideEffect.first())
        assertNull(viewModel.uiState.value.selectedDate)

        repository.records = listOf(ArchiveRecord(9, "저녁", null), ArchiveRecord(2, "아침", "photo"))
        viewModel.onDateClick(date)
        assertEquals(listOf(9L, 2L), viewModel.uiState.value.records.map { it.activityId })
        assertEquals(date, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun offlineCardClickKeepsSheetAndDoesNotNavigate() = runTest {
        val repository = FakeRepository().apply {
            records = listOf(ArchiveRecord(1, "첫 기록", null), ArchiveRecord(2, "둘째 기록", null))
        }
        val viewModel = viewModel(repository)
        viewModel.onDateClick(LocalDate.now())
        repository.offline = true

        viewModel.onRecordClick(1)
        assertEquals(ArchiveSideEffect.NetworkUnavailable, viewModel.sideEffect.first())
        assertEquals(2, viewModel.uiState.value.records.size)
        assertFalse(viewModel.uiState.value.isLoadingRecords)

        repository.offline = false
        viewModel.onRecordClick(1)
        assertEquals(ArchiveSideEffect.NavigateToDetail(1), viewModel.sideEffect.first())
    }

    @Test
    fun emptyResultDoesNotOpenSheetAndMonthChangeCancelsPendingSelection() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        viewModel.onDateClick(LocalDate.now())
        assertTrue(viewModel.uiState.value.records.isEmpty())

        repository.slow = true
        viewModel.onDateClick(LocalDate.now())
        runCurrent()
        viewModel.onPreviousMonthClick()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.isLoadingRecords)
    }

    @Test
    fun emptyDailyResultRefreshesSummaryAndClosesExistingSheet() = runTest {
        val repository = FakeRepository().apply {
            summaries = listOf(DailyActivitySummary(LocalDate.now(), 60, 2, null))
            records = listOf(ArchiveRecord(1, "첫 기록", null), ArchiveRecord(2, "둘째 기록", null))
        }
        val viewModel = viewModel(repository)
        viewModel.onDateClick(LocalDate.now())
        assertEquals(2, viewModel.uiState.value.records.size)
        repository.records = emptyList()
        repository.summaries = emptyList()

        viewModel.onRecordClick(1)

        assertEquals(2, repository.summaryCalls)
        assertNull(viewModel.uiState.value.selectedDate)
        assertTrue(viewModel.uiState.value.records.isEmpty())
        assertTrue((viewModel.uiState.value.content as UiState.Success).data.calendarDays.isEmpty())
        assertFalse(viewModel.uiState.value.isLoadingRecords)
    }

    private fun viewModel(repository: ArchiveRepository) = ArchiveViewModel(
        repository,
        object : MemberRepository {
            override suspend fun getMemberSummary() = Result.success(
                MemberSummary("이름", null, LocalDate.now().minusMonths(2), 60, 2),
            )
        },
        SavedStateHandle(),
    )

    private class FakeRepository : ArchiveRepository {
        var records = emptyList<ArchiveRecord>()
        var summaries = emptyList<DailyActivitySummary>()
        var summaryCalls = 0
        var offline = false
        var slow = false
        override suspend fun getMonthlySummary(month: YearMonth): Result<List<DailyActivitySummary>> {
            summaryCalls++
            return Result.success(summaries)
        }
        override suspend fun getDailyRecords(date: LocalDate): Result<List<ArchiveRecord>> {
            if (slow) withContext(NonCancellable) { delay(100) }
            return if (offline) Result.failure(ApiException.NetworkConnection()) else Result.success(records)
        }
    }
}
