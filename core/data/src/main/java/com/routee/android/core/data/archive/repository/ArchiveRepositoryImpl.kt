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
    override suspend fun getDailyRecords(date: LocalDate): Result<List<ArchiveRecord>> =
        apiResponseHandler.safeApiCall { service.getDailyRecords(date.toString()) }
            .map { response -> response.activities.map { ArchiveRecord(it.activityId, it.title, it.thumbnailUrl) } }

    override suspend fun getMonthlySummary(month: YearMonth): Result<List<DailyActivitySummary>> =
        apiResponseHandler.safeApiCall { service.getMonthlySummary(month.year, month.monthValue) }
            .mapCatching { response ->
                response.result.map { day ->
                    DailyActivitySummary(
                        date = LocalDate.parse(day.activityDate),
                        totalDurationMinutes = day.totalDurationMinutes,
                        activityCount = day.activityCount,
                        coverImageUrl = day.coverImageUrl,
                    )
                }
            }
}
