package com.routee.android.core.data.timeline.remote.service

import com.routee.android.core.data.timeline.remote.dto.request.ActivityCompleteRequest
import com.routee.android.core.data.timeline.remote.dto.request.RoutesCreateRequest
import com.routee.android.core.network.model.BaseResponse
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface TimelineCompletionService {
    @PUT("api/v1/activity/{activityId}")
    suspend fun complete(
        @Path("activityId") activityId: Long,
        @Body request: ActivityCompleteRequest,
    ): BaseResponse<JsonElement>

    @POST("api/v1/activity/{activityId}/route")
    suspend fun createRoutes(
        @Path("activityId") activityId: Long,
        @Body request: RoutesCreateRequest,
    ): BaseResponse<JsonElement>
}
