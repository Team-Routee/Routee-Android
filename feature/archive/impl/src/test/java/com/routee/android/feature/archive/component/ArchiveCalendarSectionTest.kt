package com.routee.android.feature.archive.component

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArchiveCalendarSectionTest {
    @Test
    fun leapMonthStartsOnThursdayAndKeepsPhotoAndBadgeData() {
        val recordedDay = CalendarDay(29, recordCount = 12, photoUrl = "photo")
        val days = calendarDaysForMonth(YearMonth.of(2024, 2), listOf(recordedDay, CalendarDay(31)))

        assertEquals(32, days.size)
        repeat(3) { assertNull(days[it]) }
        assertEquals(CalendarDay(1), days[3])
        assertEquals(recordedDay, days.last())
    }

    @Test
    fun sundayMonthLeavesSixLeadingCells() {
        val days = calendarDaysForMonth(YearMonth.of(2026, 3), emptyList())

        assertEquals(37, days.size)
        repeat(6) { assertNull(days[it]) }
        assertEquals(CalendarDay(1), days[6])
        assertEquals(CalendarDay(31), days.last())
    }
}
