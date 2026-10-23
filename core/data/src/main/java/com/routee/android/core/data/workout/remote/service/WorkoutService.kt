package com.routee.android.core.data.workout.remote.service

import com.routee.android.core.data.workout.remote.dto.request.CreateActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateActivityStatusRequest
import com.routee.android.core.data.workout.remote.dto.response.CreateActivityResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
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
}
