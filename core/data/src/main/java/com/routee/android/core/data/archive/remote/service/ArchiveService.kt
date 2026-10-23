package com.routee.android.core.data.archive.remote.service

import com.routee.android.core.data.archive.remote.dto.response.ActivityStatisticsResponse
import com.routee.android.core.data.archive.remote.dto.response.ArchiveRoutesResponse
import com.routee.android.core.data.archive.remote.dto.response.ArchiveTimelineResponse
import com.routee.android.core.data.archive.remote.dto.response.MonthlyActivitySummaryResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ArchiveService {
    @GET("api/v1/activity/{activityId}/statistics")
    suspend fun getStatistics(@Path("activityId") activityId: Long): BaseResponse<ActivityStatisticsResponse>

    @GET("api/v1/activity/{activityId}/timeline")
    suspend fun getTimeline(@Path("activityId") activityId: Long): BaseResponse<ArchiveTimelineResponse>

    @GET("api/v1/activity/{activityId}/route")
    suspend fun getRoutes(@Path("activityId") activityId: Long): BaseResponse<ArchiveRoutesResponse>

    @GET("api/v1/archive/activity-summary")
    suspend fun getMonthlySummary(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): BaseResponse<MonthlyActivitySummaryResponse>
}
