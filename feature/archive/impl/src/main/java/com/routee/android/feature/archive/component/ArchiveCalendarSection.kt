package com.routee.android.feature.archive.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun ArchiveCalendarSection(
    month: YearMonth,
    days: List<CalendarDay>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = remember(month, days) { calendarDaysForMonth(month, days) }
    Calendar(days = cells, modifier = modifier, onDayClick = { onDateClick(month.atDay(it.day)) })
}

internal fun calendarDaysForMonth(month: YearMonth, days: List<CalendarDay>): List<CalendarDay?> {
    val daysByNumber = days.associateBy(CalendarDay::day)
    return List(month.atDay(1).dayOfWeek.value - 1) { null } + (1..month.lengthOfMonth()).map { day ->
        daysByNumber[day] ?: CalendarDay(day)
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveCalendarSectionPreview() {
    RouteePreviewTheme {
        ArchiveCalendarSection(
            month = YearMonth.of(2026, 3),
            days = listOf(CalendarDay(12, recordCount = 1), CalendarDay(22, recordCount = 2)),
            onDateClick = {},
        )
    }
}
