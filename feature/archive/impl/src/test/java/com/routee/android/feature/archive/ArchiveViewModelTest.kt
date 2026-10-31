package com.routee.android.feature.archive

import androidx.lifecycle.SavedStateHandle
import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.core.data.archive.repository.ArchiveRepository
import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.component.MountainActivityLevel
import com.routee.android.feature.archive.model.ArchiveContent
import com.routee.android.feature.archive.model.ArchiveSideEffect
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun changedRecordRefreshesMemberSummaryWithoutChangingSelectedMonth() = runTest {
        val repository = FakeArchiveRepository()
        val members = FakeMemberRepository()
        val viewModel = ArchiveViewModel(repository, members, SavedStateHandle())
        viewModel.onPreviousMonthClick()
        val month = viewModel.uiState.value.selectedMonth

        viewModel.onRecordChanged()

        assertEquals(2, members.callCount)
        assertEquals(month, viewModel.content().month)
        assertEquals(3, repository.months.size)
    }

    @Test
    fun monthMovementHonorsJoinAndCurrentMonthsWithoutReloadingMember() = runTest {
        val repository = FakeArchiveRepository()
        val members = FakeMemberRepository()
        val viewModel = ArchiveViewModel(repository, members, SavedStateHandle())
        val current = viewModel.uiState.value.currentMonth

        repeat(3) { viewModel.onPreviousMonthClick() }
        assertEquals(current.minusMonths(2), viewModel.uiState.value.selectedMonth)
        repeat(3) { viewModel.onNextMonthClick() }

        assertEquals(current, viewModel.uiState.value.selectedMonth)
        assertFalse(viewModel.uiState.value.isNextMonthEnabled)
        assertEquals(1, members.callCount)
        assertEquals(5, repository.months.size)
    }

    @Test
    fun offlineMonthMovementClearsDifferentMonthAndRetryUpdatesIt() = runTest {
        val repository = FakeArchiveRepository { month ->
            Result.success(listOf(DailyActivitySummary(month.atDay(12), 90, 2, "photo")))
        }
        val viewModel = ArchiveViewModel(repository, FakeMemberRepository(), SavedStateHandle())
        val previousContent = viewModel.content()
        assertTrue(previousContent.calendarDays.isNotEmpty())
        repository.error = ApiException.NetworkConnection()

        viewModel.onPreviousMonthClick()

        assertEquals(viewModel.uiState.value.selectedMonth, viewModel.content().month)
        assertEquals(previousContent.profile, viewModel.content().profile)
        assertTrue(viewModel.content().calendarDays.isEmpty())
        assertTrue(viewModel.content().activityLevels.all { it == MountainActivityLevel.NONE })
        assertTrue(viewModel.uiState.value.isOffline)
        assertEquals(ArchiveSideEffect.NetworkUnavailable, viewModel.sideEffect.first())

        repository.error = null
        viewModel.onRetryClick()

        assertFalse(viewModel.uiState.value.isOffline)
        assertEquals(viewModel.uiState.value.selectedMonth, viewModel.content().month)
        assertEquals(2, viewModel.content().calendarDays.single().recordCount)
    }

    @Test
    fun offlineRetryOfSameMonthKeepsLoadedRecords() = runTest {
        val repository = FakeArchiveRepository { month ->
            Result.success(listOf(DailyActivitySummary(month.atDay(12), 90, 2, "photo")))
        }
        val viewModel = ArchiveViewModel(repository, FakeMemberRepository(), SavedStateHandle())
        val previousContent = viewModel.uiState.value.content
        repository.error = ApiException.NetworkConnection()

        viewModel.onRetryClick()

        assertEquals(previousContent, viewModel.uiState.value.content)
        assertTrue(viewModel.uiState.value.isOffline)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun slowerCancelledMonthDoesNotOverwriteNewSelection() = runTest {
        val current = YearMonth.now()
        val repository = FakeArchiveRepository { month ->
            if (month == current.minusMonths(1)) withContext(NonCancellable) { delay(100) }
            Result.success(emptyList())
        }
        val viewModel = ArchiveViewModel(repository, FakeMemberRepository(), SavedStateHandle())

        viewModel.onPreviousMonthClick()
        runCurrent()
        viewModel.onPreviousMonthClick()
        advanceUntilIdle()

        assertEquals(current.minusMonths(2), viewModel.content().month)
    }

    @Test
    fun initialOfflineEntryShowsEmptyContentAndCanBeRetried() = runTest {
        val repository = FakeArchiveRepository().apply { error = ApiException.NetworkConnection() }
        val restored = YearMonth.now().minusMonths(1)
        val viewModel = ArchiveViewModel(
            repository,
            FakeMemberRepository(),
            SavedStateHandle(mapOf("selectedMonth" to restored.toString())),
        )
        assertEquals(UiState.Empty, viewModel.uiState.value.content)
        assertTrue(viewModel.uiState.value.isOffline)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(ArchiveSideEffect.NetworkUnavailable, viewModel.sideEffect.first())

        repository.error = null
        viewModel.onRetryClick()

        assertEquals(restored, viewModel.content().month)
        assertTrue(viewModel.uiState.value.content is UiState.Success)
    }

    @Test
    fun initialMemberNetworkFailureAlsoShowsOfflineContent() = runTest {
        val members = FakeMemberRepository().apply { error = ApiException.NetworkConnection() }
        val viewModel = ArchiveViewModel(FakeArchiveRepository(), members, SavedStateHandle())

        assertEquals(UiState.Empty, viewModel.uiState.value.content)
        assertTrue(viewModel.uiState.value.isOffline)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(ArchiveSideEffect.NetworkUnavailable, viewModel.sideEffect.first())

        members.error = null
        viewModel.onRetryClick()

        assertTrue(viewModel.uiState.value.content is UiState.Success)
        assertFalse(viewModel.uiState.value.isOffline)
    }

    @Test
    fun nonNetworkFailureStillShowsErrorScreen() = runTest {
        val repository = FakeArchiveRepository().apply { error = IllegalStateException() }
        val viewModel = ArchiveViewModel(repository, FakeMemberRepository(), SavedStateHandle())

        assertTrue(viewModel.uiState.value.content is UiState.Failure)
        assertFalse(viewModel.uiState.value.isOffline)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    private fun ArchiveViewModel.content() = (uiState.value.content as UiState.Success<ArchiveContent>).data

    private class FakeMemberRepository : MemberRepository {
        var callCount = 0
        var error: Exception? = null

        override suspend fun getMemberSummary(): Result<MemberSummary> {
            callCount++
            error?.let { return Result.failure(it) }
            return Result.success(MemberSummary("이름", null, YearMonth.now().minusMonths(2).atDay(1), 70, 5))
        }
    }

    private class FakeArchiveRepository(
        private val load: suspend (YearMonth) -> Result<List<DailyActivitySummary>> = { Result.success(emptyList()) },
    ) : ArchiveRepository {
        override suspend fun getDailyRecords(date: java.time.LocalDate) = error("Unused")
        var error: Exception? = null
        val months = mutableListOf<YearMonth>()

        override suspend fun getMonthlySummary(month: YearMonth): Result<List<DailyActivitySummary>> {
            months += month
            error?.let { return Result.failure(it) }
            return load(month)
        }
    }
}
