package com.routee.android.core.data.archive.repository

import com.routee.android.core.data.archive.model.ArchiveRecord
import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.core.data.archive.remote.service.ArchiveService
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

internal class ArchiveRepositoryImpl @Inject constructor(
    private val service: ArchiveService,
    private val apiResponseHandler: ApiResponseHandler,
) : ArchiveRepository {
    private val cacheLock = Any()
    private var cacheVersion = 0
    private var cachedMonth: Pair<YearMonth, List<DailyActivitySummary>>? = null

    override fun getCachedMonthlySummary(month: YearMonth): List<DailyActivitySummary>? = synchronized(cacheLock) {
        cachedMonth?.takeIf { it.first == month }?.second
    }

    override fun clearCache() = synchronized(cacheLock) {
        cacheVersion++
        cachedMonth = null
    }

    override suspend fun getDailyRecords(date: LocalDate): Result<List<ArchiveRecord>> =
        apiResponseHandler.safeApiCall { service.getDailyRecords(date.toString()) }
            .map { response -> response.activities.map { ArchiveRecord(it.activityId, it.title, it.thumbnailUrl) } }

    override suspend fun getMonthlySummary(month: YearMonth): Result<List<DailyActivitySummary>> {
        val version = synchronized(cacheLock) { cacheVersion }
        return apiResponseHandler.safeApiCall { service.getMonthlySummary(month.year, month.monthValue) }
            .mapCatching { response ->
                response.result.map { day ->
                    DailyActivitySummary(
                        date = LocalDate.parse(day.activityDate),
                        totalDurationMinutes = day.totalDurationMinutes,
                        activityCount = day.activityCount,
                        coverImageUrl = day.coverImageUrl,
                    )
                }
            }.onSuccess { summaries ->
                synchronized(cacheLock) {
                    if (version == cacheVersion) cachedMonth = month to summaries
                }
            }
    }
}
