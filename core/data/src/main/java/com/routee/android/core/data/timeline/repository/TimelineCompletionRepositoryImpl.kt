package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelineCompletion
import com.routee.android.core.data.timeline.remote.dto.request.ActivityCompleteRequest
import com.routee.android.core.data.timeline.remote.dto.request.ActivityTrackPointRequest
import com.routee.android.core.data.timeline.remote.dto.request.RouteRequest
import com.routee.android.core.data.timeline.remote.dto.request.RoutesCreateRequest
import com.routee.android.core.data.timeline.remote.service.TimelineCompletionService
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

internal class TimelineCompletionRepositoryImpl @Inject constructor(
    private val service: TimelineCompletionService,
    private val apiResponseHandler: ApiResponseHandler,
) : TimelineCompletionRepository {
    override suspend fun complete(input: TimelineCompletion, mapImageObjectKey: String): Result<Unit> {
        val title = input.title.trim()
        val names = input.routeNames.map(String::trim).filter(String::isNotEmpty)
        if (title.isEmpty() || title.length > 16 || input.distanceMeters < 0 || input.durationSeconds <= 0 ||
            input.maxElevationMeters !in -500..9000 || mapImageObjectKey.isBlank() || input.track.size < 2 ||
            input.track.any { it.latitude !in -90.0..90.0 || it.longitude !in -180.0..180.0 } ||
            names.size > 20 || names.any { it.length > 16 }
        ) {
            return Result.failure(IllegalArgumentException("올바르지 않은 운동 기록"))
        }

        val request = ActivityCompleteRequest(
            title = title,
            distance = input.distanceMeters,
            durationSec = input.durationSeconds,
            maxElevation = input.maxElevationMeters,
            mapImageObjectKey = mapImageObjectKey,
            track = input.track.map { point ->
                ActivityTrackPointRequest(point.latitude, point.longitude, point.elevation, point.pointIndex)
            },
            endedAt = Instant.ofEpochMilli(input.endedAtEpochMillis)
                .atZone(ZoneId.systemDefault()).toLocalDateTime().toString(),
        )
        apiResponseHandler.safeUnitApiCall { service.complete(input.activityId, request) }.exceptionOrNull()
            ?.let { return Result.failure(it) }
        if (names.isEmpty()) return Result.success(Unit)
        return apiResponseHandler.safeUnitApiCall {
            service.createRoutes(
                input.activityId,
                RoutesCreateRequest(names.mapIndexed { index, name -> RouteRequest(name, index + 1) }),
            )
        }
    }
}
