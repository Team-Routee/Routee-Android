package com.routee.android.core.data.workout.remote.service

import com.routee.android.core.data.workout.remote.dto.request.CompleteActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.CreateActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateActivityStatusRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateRoutesRequest
import com.routee.android.core.data.workout.remote.dto.response.CreateActivityResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface WorkoutService {
    @POST("api/v1/activity")
    suspend fun createWorkout(
        @Body request: CreateActivityRequest,
    ): BaseResponse<CreateActivityResponse>

    @PATCH("api/v1/activity/{activityId}/status")
    suspend fun updateWorkoutStatus(
        @Path("activityId") activityId: Long,
        @Body request: UpdateActivityStatusRequest,
    )

    @PUT("api/v1/activity/{activityId}")
    suspend fun completeWorkout(
        @Path("activityId") activityId: Long,
        @Body request: CompleteActivityRequest,
    )

    @PUT("api/v1/activity/{activityId}/route")
    suspend fun updateRoutes(
        @Path("activityId") activityId: Long,
        @Body request: UpdateRoutesRequest,
    )
}
