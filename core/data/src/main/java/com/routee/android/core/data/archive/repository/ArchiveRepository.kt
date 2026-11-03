package com.routee.android.core.data.archive.repository

import com.routee.android.core.data.archive.model.ArchiveRecord
import com.routee.android.core.data.archive.model.DailyActivitySummary
import java.time.LocalDate
import java.time.YearMonth

/** 선택한 월의 일별 운동 요약 조회 */
interface ArchiveRepository {
    /** 마지막 조회 월과 일치하는 월간 요약 캐시, 없으면 null */
    fun getCachedMonthlySummary(month: YearMonth): List<DailyActivitySummary>?

    /** 월간 요약 캐시 삭제 */
    fun clearCache()

    suspend fun getDailyRecords(date: LocalDate): Result<List<ArchiveRecord>>

    suspend fun getMonthlySummary(month: YearMonth): Result<List<DailyActivitySummary>>
}
