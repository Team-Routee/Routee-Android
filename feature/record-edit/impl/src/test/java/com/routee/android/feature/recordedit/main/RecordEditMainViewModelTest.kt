package com.routee.android.feature.recordedit.main

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.data.member.repository.MemberRepository
import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.FakeRecapRepository
import com.routee.android.feature.recordedit.MainDispatcherRule
import com.routee.android.feature.recordedit.main.model.RecordEditMainSideEffect
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ORIGINAL_TITLE = "2026.07.14 기록"

class RecordEditMainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recapRepository = FakeRecapRepository()
    private val memberRepository = FakeMemberRepository()

    private fun createViewModel() =
        RecordEditMainViewModel(recapRepository, memberRepository)

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

    @Test
    fun `제목을 바꾸고 편집을 끝내면 수정 요청을 보낸다`() {
        val viewModel = createViewModelWithRecap()

        editTitle(viewModel, "관악산")

        assertEquals(listOf(1L to "관악산"), recapRepository.updatedTitles)
        assertEquals("관악산", titleOf(viewModel))
    }

    @Test
    fun `제목이 그대로거나 공백뿐이면 수정 요청을 보내지 않는다`() {
        val viewModel = createViewModelWithRecap()

        editTitle(viewModel, ORIGINAL_TITLE)
        editTitle(viewModel, "   ")

        assertTrue(recapRepository.updatedTitles.isEmpty())
        assertEquals(ORIGINAL_TITLE, titleOf(viewModel))
    }

    @Test
    fun `제목 수정에 실패하면 원래 제목으로 되돌린다`() {
        recapRepository.titleError = IOException()
        val viewModel = createViewModelWithRecap()

        editTitle(viewModel, "관악산")

        assertEquals(ORIGINAL_TITLE, titleOf(viewModel))
    }

    @Test
    fun `카드를 누르면 바로 편집 화면으로 이동한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onRecapClick(1)

        assertEquals(RecordEditMainSideEffect.NavigateToEditor(1), viewModel.sideEffect.first())
    }

    @Test
    fun `제목을 연달아 바꾸고 모두 실패하면 서버에 저장된 제목으로 되돌린다`() {
        recapRepository.titleError = IOException()
        recapRepository.titleResponseGate = CompletableDeferred()
        val viewModel = createViewModelWithRecap()

        editTitle(viewModel, "관악산")
        editTitle(viewModel, "북한산")
        recapRepository.titleResponseGate?.complete(Unit)

        assertEquals(ORIGINAL_TITLE, titleOf(viewModel))
    }

    @Test
    fun `먼저 바꾼 제목만 저장되고 다음 제목이 실패하면 먼저 저장된 제목으로 되돌린다`() {
        val viewModel = createViewModelWithRecap()
        editTitle(viewModel, "관악산")

        recapRepository.titleError = IOException()
        editTitle(viewModel, "북한산")

        assertEquals("관악산", titleOf(viewModel))
    }

    private fun createViewModelWithRecap(): RecordEditMainViewModel {
        recapRepository.recaps = listOf(recap(activityId = 1, date = LocalDate.of(2026, 7, 14), photoCount = 0))
        return createViewModel()
    }

    private fun editTitle(
        viewModel: RecordEditMainViewModel,
        title: String,
    ) {
        viewModel.onEditTitleClick(1)
        viewModel.titleEditingState.setTextAndPlaceCursorAtEnd(title)
        viewModel.onEditTitleComplete(1)
    }

    private fun titleOf(viewModel: RecordEditMainViewModel): String =
        (viewModel.uiState.value.recaps as UiState.Success).data.first { it.activityId == 1L }.title

    private fun recap(
        activityId: Long,
        date: LocalDate,
        photoCount: Int,
    ) = ActivityRecap(
        activityId = activityId,
        title = ORIGINAL_TITLE,
        activityDate = date,
        timelineImageUrls = List(photoCount) { "https://example.com/$it.jpg" },
    )
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
