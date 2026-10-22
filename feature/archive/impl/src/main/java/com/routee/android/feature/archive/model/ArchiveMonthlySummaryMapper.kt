package com.routee.android.feature.archive.model

import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.feature.archive.component.CalendarDay
import com.routee.android.feature.archive.component.MOUNTAIN_MAP_CELL_COUNT
import com.routee.android.feature.archive.component.MountainActivityLevel
import java.time.YearMonth
import kotlinx.collections.immutable.toImmutableList

internal fun archiveContent(
    profile: ArchiveProfile,
    month: YearMonth,
    summaries: List<DailyActivitySummary>,
): ArchiveContent {
    val days = summaries.filter { YearMonth.from(it.date) == month }
    val byDay = days.associateBy { it.date.dayOfMonth }
    return ArchiveContent(
        profile = profile,
        month = month,
        calendarDays = days.map {
            CalendarDay(it.date.dayOfMonth, it.activityCount, it.coverImageUrl)
        }.toImmutableList(),
        activityLevels = List(MOUNTAIN_MAP_CELL_COUNT) { index ->
            byDay[index + 1]?.activityLevel() ?: MountainActivityLevel.NONE
        }
            .toImmutableList(),
    )
}

private fun DailyActivitySummary.activityLevel(): MountainActivityLevel = when {
    activityCount <= 0 -> MountainActivityLevel.NONE
    totalDurationMinutes <= 60 -> MountainActivityLevel.LOW
    totalDurationMinutes <= 120 -> MountainActivityLevel.MEDIUM
    totalDurationMinutes <= 180 -> MountainActivityLevel.HIGH
    else -> MountainActivityLevel.VERY_HIGH
}
