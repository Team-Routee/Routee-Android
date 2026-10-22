package com.routee.android.feature.archive.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val SheetDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Immutable
internal data class ArchiveCalendarRecord(
    val activityId: Long,
    val title: String,
    val startedAt: LocalDateTime,
    val thumbnailUrl: String? = null,
)

@Composable
internal fun ArchiveCalendarSection(
    month: YearMonth,
    days: List<CalendarDay>,
    records: List<ArchiveCalendarRecord>,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = remember(month, days) { calendarDaysForMonth(month, days) }
    val recordsByDay = remember(month, records) { recordsForMonth(month, records) }
    var selectedDay by rememberSaveable(month.toString()) { mutableStateOf<Int?>(null) }

    Calendar(
        days = cells,
        modifier = modifier,
        onDayClick = { day ->
            val selectedRecords = recordsByDay[day.day].orEmpty()
            when (selectedRecords.size) {
                0 -> Unit
                1 -> onRecordClick(selectedRecords.single().activityId)
                else -> selectedDay = day.day
            }
        },
    )
    val sheetDay = selectedDay
    val sheetRecords = recordsByDay[sheetDay].orEmpty()
    if (sheetDay != null && sheetRecords.size > 1) {
        RecordBottomSheet(
            date = month.atDay(sheetDay).format(SheetDateFormatter),
            records = sheetRecords,
            onRecordClick = { activityId ->
                selectedDay = null
                onRecordClick(activityId)
            },
            onDismissRequest = { selectedDay = null },
        )
    }
}

internal fun calendarDaysForMonth(month: YearMonth, days: List<CalendarDay>): List<CalendarDay?> {
    val daysByNumber = days.associateBy(CalendarDay::day)
    return List(month.atDay(1).dayOfWeek.value - 1) { null } + (1..month.lengthOfMonth()).map { day ->
        daysByNumber[day] ?: CalendarDay(day)
    }
}

internal fun recordsForMonth(
    month: YearMonth,
    records: List<ArchiveCalendarRecord>,
): Map<Int, List<RecordSheetItem>> = records
    .filter { YearMonth.from(it.startedAt) == month }
    .sortedByDescending(ArchiveCalendarRecord::startedAt)
    .groupBy { it.startedAt.dayOfMonth }
    .mapValues { (_, records) -> records.map { RecordSheetItem(it.activityId, it.title, it.thumbnailUrl) } }

@PreviewDeviceSizes
@Composable
private fun ArchiveCalendarSectionPreview() {
    RouteePreviewTheme {
        ArchiveCalendarSection(
            month = YearMonth.of(2026, 3),
            days = listOf(CalendarDay(12, recordCount = 1), CalendarDay(22, recordCount = 2)),
            records = listOf(
                ArchiveCalendarRecord(1, "북악산 산행", LocalDateTime.of(2026, 3, 12, 10, 0)),
                ArchiveCalendarRecord(2, "오전 산행", LocalDateTime.of(2026, 3, 22, 9, 0)),
                ArchiveCalendarRecord(3, "저녁 산책", LocalDateTime.of(2026, 3, 22, 18, 0)),
            ),
            onRecordClick = {},
        )
    }
}
