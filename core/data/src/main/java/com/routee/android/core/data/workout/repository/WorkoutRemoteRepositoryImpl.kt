package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.model.toServerElevation
import com.routee.android.core.data.workout.remote.dto.request.ActivityTrackPointRequest
import com.routee.android.core.data.workout.remote.dto.request.CompleteActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.CreateActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.RouteRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateActivityStatusRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateRoutesRequest
import com.routee.android.core.data.workout.remote.service.WorkoutService
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

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

    override suspend fun completeWorkout(
        record: WorkoutRecord,
        trackPoints: List<TrackPoint>,
        mapImageObjectKey: String,
    ): Result<Unit> = apiResponseHandler.safeUnitApiCall {
        workoutService.completeWorkout(
            activityId = record.activityId,
            request = record.toCompleteRequest(trackPoints, mapImageObjectKey),
        )
    }.recoverCatching { error ->
        if (error !is ApiException.Conflict || error.serverCode != ACTIVITY_ALREADY_COMPLETED) throw error
    }

    override suspend fun updateRoutes(
        activityId: Long,
        names: List<String>,
    ): Result<Unit> = apiResponseHandler.safeUnitApiCall {
        workoutService.updateRoutes(
            activityId = activityId,
            request = UpdateRoutesRequest(
                routes = names.mapIndexed { index, name -> RouteRequest(name = name, sequence = index + 1) },
            ),
        )
    }

    private fun WorkoutRecord.toCompleteRequest(
        trackPoints: List<TrackPoint>,
        mapImageObjectKey: String,
    ): CompleteActivityRequest {
        val endedAt = checkNotNull(endedAt) { "종료 시각 없음" }
        return CompleteActivityRequest(
            title = title,
            distance = distanceMeters.roundToInt(),
            durationSec = durationSecondsAt(endedAt),
            maxElevation = maxElevationMeters?.toServerElevation() ?: 0,
            mapImageObjectKey = mapImageObjectKey,
            track = trackPoints.map { point ->
                ActivityTrackPointRequest(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    elevation = point.elevation.toServerElevation(),
                    pointIndex = point.pointIndex,
                )
            },
            endedAt = endedAt.toLocalDateTimeString(),
        )
    }

    /** 기기 시간대의 초 단위 시각 (예: 2026-10-23T09:30:00), 시간대는 Time-Zone 헤더로 보냄 */
    private fun Instant.toLocalDateTimeString(): String = LocalDateTime
        .ofInstant(truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault())
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private companion object {
        const val ACTIVITY_ALREADY_COMPLETED = "ACTIVITY_ALREADY_COMPLETED"
    }
}
