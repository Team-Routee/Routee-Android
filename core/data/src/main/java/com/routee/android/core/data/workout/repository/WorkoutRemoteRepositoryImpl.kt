package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.remote.dto.request.CreateActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateActivityStatusRequest
import com.routee.android.core.data.workout.remote.service.WorkoutService
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

internal class WorkoutRemoteRepositoryImpl @Inject constructor(
    private val workoutService: WorkoutService,
    private val apiResponseHandler: ApiResponseHandler,
) : WorkoutRemoteRepository {
    override suspend fun createWorkout(
        type: WorkoutType,
        startedAt: Instant,
    ): Result<StartedWorkout> = apiResponseHandler.safeApiCall {
        workoutService.createWorkout(
            request = CreateActivityRequest(activityType = type.name, startedAt = startedAt.toLocalDateTimeString()),
        )
    }.map { StartedWorkout(activityId = it.activityId, title = it.title) }

    override suspend fun updateStatus(
        activityId: Long,
        status: WorkoutStatus,
    ): Result<Unit> = apiResponseHandler.safeUnitApiCall {
        workoutService.updateWorkoutStatus(
            activityId = activityId,
            request = UpdateActivityStatusRequest(status = status.name),
        )
    }

    /** 기기 시간대의 초 단위 시각 (예: 2026-10-23T09:30:00), 시간대는 Time-Zone 헤더로 보냄 */
    private fun Instant.toLocalDateTimeString(): String = LocalDateTime
        .ofInstant(truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault())
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
