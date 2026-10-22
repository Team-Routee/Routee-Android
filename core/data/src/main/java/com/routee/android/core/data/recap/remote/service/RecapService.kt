package com.routee.android.core.data.recap.remote.service

import com.routee.android.core.data.recap.remote.dto.response.MonthlyRecapResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface RecapService {
    @GET("api/v1/activity/recap")
    suspend fun getMonthlyRecaps(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): BaseResponse<MonthlyRecapResponse>
}
