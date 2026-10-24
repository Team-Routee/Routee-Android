package com.routee.android.core.data.archive.repository

import com.routee.android.core.data.archive.model.ArchiveRecord
import com.routee.android.core.data.archive.model.DailyActivitySummary
import java.time.LocalDate
import java.time.YearMonth

/** 선택한 월의 일별 운동 요약 조회 */
interface ArchiveRepository {
    suspend fun getDailyRecords(date: LocalDate): Result<List<ArchiveRecord>>

    suspend fun getMonthlySummary(month: YearMonth): Result<List<DailyActivitySummary>>
}
