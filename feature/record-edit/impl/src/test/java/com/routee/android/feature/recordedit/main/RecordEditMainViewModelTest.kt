package com.routee.android.feature.recordedit.main

import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.MainDispatcherRule
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecordEditMainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recapRepository = FakeRecapRepository()
    private val memberRepository = FakeMemberRepository()

    private fun createViewModel() = RecordEditMainViewModel(recapRepository, memberRepository)

    @Test
    fun `가입 월을 불러오면 그달에서 이전 달 이동이 막힌다`() {
        memberRepository.joinDate = LocalDate.now()

        val viewModel = createViewModel()

        assertEquals(YearMonth.now(), viewModel.uiState.value.joinedMonth)
        assertFalse(viewModel.uiState.value.isPreviousMonthEnabled)
    }

    @Test
    fun `기록은 최신 날짜 순으로 보여주고 사진은 4장까지만 쓴다`() {
        recapRepository.recaps = listOf(
            recap(activityId = 1, date = LocalDate.of(2026, 7, 13), photoCount = 5),
            recap(activityId = 2, date = LocalDate.of(2026, 7, 14), photoCount = 1),
        )

        val viewModel = createViewModel()

        val recaps = (viewModel.uiState.value.recaps as UiState.Success).data
        assertEquals(listOf(2L, 1L), recaps.map { it.activityId })
        assertEquals("2026.07.14.", recaps.first().workoutDate)
        assertEquals(4, recaps.last().thumbnailUrls.size)
    }

    @Test
    fun `기록이 없는 달은 빈 상태다`() {
        val viewModel = createViewModel()

        assertEquals(UiState.Empty, viewModel.uiState.value.recaps)
    }

    @Test
    fun `목록 조회에 실패하면 실패 상태다`() {
        recapRepository.error = IOException()

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.recaps is UiState.Failure)
    }

    @Test
    fun `가입 월 조회에 실패하면 실패 상태고 다시 시도하면 불러온다`() {
        memberRepository.error = IOException()
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.recaps is UiState.Failure)

        memberRepository.error = null
        viewModel.onRetryClick()

        assertEquals(UiState.Empty, viewModel.uiState.value.recaps)
        assertEquals(YearMonth.from(memberRepository.joinDate), viewModel.uiState.value.joinedMonth)
    }

    @Test
    fun `달을 옮기면 그달 기록을 다시 불러온다`() {
        memberRepository.joinDate = LocalDate.now().minusYears(1)
        val viewModel = createViewModel()

        viewModel.onPreviousMonthClick()

        assertEquals(YearMonth.now().minusMonths(1), recapRepository.requestedMonths.last())
    }

    private fun recap(
        activityId: Long,
        date: LocalDate,
        photoCount: Int,
    ) = ActivityRecap(
        activityId = activityId,
        title = "${date}의 기록",
        activityDate = date,
        timelineImageUrls = List(photoCount) { "https://example.com/$it.jpg" },
    )
}

private class FakeRecapRepository : RecapRepository {
    var recaps: List<ActivityRecap> = emptyList()
    var error: Throwable? = null
    val requestedMonths = mutableListOf<YearMonth>()
    var titleError: Throwable? = null
    val updatedTitles = mutableListOf<Pair<Long, String>>()

    override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> {
        requestedMonths += month
        return error?.let { Result.failure(it) } ?: Result.success(recaps)
    }

    override suspend fun updateRecapTitle(
        activityId: Long,
        title: String,
    ): Result<Unit> {
        updatedTitles += activityId to title
        return titleError?.let { Result.failure(it) } ?: Result.success(Unit)
    }
}

private class FakeMemberRepository : MemberRepository {
    var joinDate: LocalDate = LocalDate.of(2026, 1, 1)
    var error: Throwable? = null

    override suspend fun getMemberSummary(): Result<MemberSummary> = error?.let { Result.failure(it) }
        ?: Result.success(
            MemberSummary(
                nickname = "관악산 날다람쥐",
                profileImageUrl = null,
                joinDate = joinDate,
                daysSinceJoining = 0,
                totalActivityCount = 0,
            ),
        )
}
