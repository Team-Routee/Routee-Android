package com.routee.android.feature.recordedit.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.model.RecapItemUiModel
import java.time.YearMonth
import kotlinx.collections.immutable.ImmutableList

/**
 * 기록 편집 메인 화면 상태
 *
 * @param selectedMonth     보고 있는 달
 * @param currentMonth      이번 달. 다음 달 이동 한계
 * @param joinedMonth       가입한 달. 이전 달 이동 한계. null 이면 제한 없음
 * @param recaps            그달 기록 (GET /api/v1/activity/recap). 최신 날짜 순
 * @param editingActivityId 제목을 편집 중인 기록. null 이면 편집 중이 아님
 */
@Immutable
internal data class RecordEditMainUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val joinedMonth: YearMonth? = null,
    val recaps: UiState<ImmutableList<RecapItemUiModel>> = UiState.Loading,
    val editingActivityId: Long? = null,
) {
    val isPreviousMonthEnabled: Boolean get() = joinedMonth == null || selectedMonth > joinedMonth

    val isNextMonthEnabled: Boolean get() = selectedMonth < currentMonth
}
