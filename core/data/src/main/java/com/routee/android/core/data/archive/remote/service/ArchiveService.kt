package com.routee.android.core.data.archive.remote.service

import com.routee.android.core.data.archive.remote.dto.response.MonthlyActivitySummaryResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ArchiveService {
    @GET("api/v1/archive/activity-summary")
    suspend fun getMonthlySummary(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): BaseResponse<MonthlyActivitySummaryResponse>
}
