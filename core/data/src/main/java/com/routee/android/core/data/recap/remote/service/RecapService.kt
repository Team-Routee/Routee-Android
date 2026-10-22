package com.routee.android.core.data.recap.remote.service

import com.routee.android.core.data.recap.remote.dto.request.UpdateRecapTitleRequest
import com.routee.android.core.data.recap.remote.dto.response.ActivityTrackResponse
import com.routee.android.core.data.recap.remote.dto.response.MonthlyRecapResponse
import com.routee.android.core.data.recap.remote.dto.response.RecapResourceResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

internal interface RecapService {
    @GET("api/v1/activity/recap")
    suspend fun getMonthlyRecaps(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): BaseResponse<MonthlyRecapResponse>

    @PATCH("api/v1/activity/{activityId}/title")
    suspend fun updateRecapTitle(
        @Path("activityId") activityId: Long,
        @Body request: UpdateRecapTitleRequest,
    )

    @GET("api/v1/activity/{activityId}/recap")
    suspend fun getRecapResource(
        @Path("activityId") activityId: Long,
    ): BaseResponse<RecapResourceResponse>

    @GET("api/v1/activity/{activityId}/track")
    suspend fun getActivityTrack(
        @Path("activityId") activityId: Long,
    ): BaseResponse<ActivityTrackResponse>
}
