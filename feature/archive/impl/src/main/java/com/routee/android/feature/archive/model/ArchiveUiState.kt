package com.routee.android.feature.archive.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.ui.UiState
import com.routee.android.feature.archive.component.CalendarDay
import com.routee.android.feature.archive.component.MountainActivityLevel
import java.time.YearMonth
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class ArchiveUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedMonth: YearMonth = currentMonth,
    val content: UiState<ArchiveContent> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
) {
    val isPreviousMonthEnabled: Boolean
        get() = (content as? UiState.Success)?.data?.profile?.joinedMonth?.let { selectedMonth > it } ?: false

    val isNextMonthEnabled: Boolean get() = selectedMonth < currentMonth
}

@Immutable
internal data class ArchiveProfile(
    val nickname: String,
    val daysTogether: Int,
    val workoutCount: Int,
    val joinedMonth: YearMonth,
    val imageUrl: String?,
)

@Immutable
internal data class ArchiveContent(
    val profile: ArchiveProfile,
    val month: YearMonth,
    val calendarDays: ImmutableList<CalendarDay>,
    val activityLevels: ImmutableList<MountainActivityLevel>,
)

internal fun MemberSummary.toArchiveProfile() = ArchiveProfile(
    nickname = nickname,
    daysTogether = daysSinceJoining,
    workoutCount = totalActivityCount,
    joinedMonth = YearMonth.from(joinDate),
    imageUrl = profileImageUrl,
)

internal enum class ArchiveSideEffect { NetworkUnavailable }
