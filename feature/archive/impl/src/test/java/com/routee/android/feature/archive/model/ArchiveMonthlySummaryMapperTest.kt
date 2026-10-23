package com.routee.android.feature.archive.model

import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.feature.archive.component.MountainActivityLevel
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveMonthlySummaryMapperTest {
    @Test
    fun recordCountAndMinuteBoundariesChooseFiveLevels() {
        val month = YearMonth.of(2024, 2)
        val minutes = listOf(0, 0, 60, 61, 120, 121, 180, 181)
        val summaries = minutes.mapIndexed { index, duration ->
            DailyActivitySummary(month.atDay(index + 1), duration, if (index == 0) 0 else 1, null)
        }
        val content = archiveContent(ArchiveProfile("이름", 1, 7, month, null), month, summaries)

        assertEquals(
            listOf(
                MountainActivityLevel.NONE,
                MountainActivityLevel.LOW,
                MountainActivityLevel.LOW,
                MountainActivityLevel.MEDIUM,
                MountainActivityLevel.MEDIUM,
                MountainActivityLevel.HIGH,
                MountainActivityLevel.HIGH,
                MountainActivityLevel.VERY_HIGH,
            ),
            content.activityLevels.take(8),
        )
        assertEquals(33, content.activityLevels.size)
        assertEquals(MountainActivityLevel.NONE, content.activityLevels.last())
    }

    @Test
    fun leapDayKeepsBadgeAndThumbnailWhileOtherMonthsAreIgnored() {
        val month = YearMonth.of(2024, 2)
        val content = archiveContent(
            ArchiveProfile("이름", 1, 12, month, null),
            month,
            listOf(
                DailyActivitySummary(month.atEndOfMonth(), 181, 12, "photo"),
                DailyActivitySummary(month.plusMonths(1).atDay(1), 90, 1, null),
            ),
        )

        assertEquals(29, content.calendarDays.single().day)
        assertEquals(12, content.calendarDays.single().recordCount)
        assertEquals("photo", content.calendarDays.single().photoUrl)
        assertEquals(MountainActivityLevel.VERY_HIGH, content.activityLevels[28])
        assertEquals(MountainActivityLevel.NONE, content.activityLevels[29])
    }
}
