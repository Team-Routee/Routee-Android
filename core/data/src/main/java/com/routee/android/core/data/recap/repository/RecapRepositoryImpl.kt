package com.routee.android.core.data.recap.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.recap.exception.RecapException
import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapEditorData
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.model.RecapRoute
import com.routee.android.core.data.recap.model.TimelineMarker
import com.routee.android.core.data.recap.model.TrackPoint
import com.routee.android.core.data.recap.remote.dto.request.UpdateRecapTitleRequest
import com.routee.android.core.data.recap.remote.dto.response.ActivityRecapResponse
import com.routee.android.core.data.recap.remote.dto.response.ActivityTrackResponse
import com.routee.android.core.data.recap.remote.dto.response.RecapResourceResponse
import com.routee.android.core.data.recap.remote.service.RecapService
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.extension.mapApiException
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class RecapRepositoryImpl @Inject constructor(
    private val recapService: RecapService,
    private val apiResponseHandler: ApiResponseHandler,
) : RecapRepository {

    override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> =
        apiResponseHandler.safeApiCall {
            recapService.getMonthlyRecaps(year = month.year, month = month.monthValue)
        }.mapCatching { response -> response.activities.map { it.toActivityRecap() } }

    override suspend fun updateRecapTitle(
        activityId: Long,
        title: String,
    ): Result<Unit> = apiResponseHandler.safeUnitApiCall {
        recapService.updateRecapTitle(activityId = activityId, request = UpdateRecapTitleRequest(title = title))
    }

    override suspend fun getRecapResource(activityId: Long): Result<RecapResource> =
        apiResponseHandler.safeApiCall { recapService.getRecapResource(activityId) }
            .map { it.toRecapResource() }
            .mapApiException(::mapRecapException)

    override suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack> =
        apiResponseHandler.safeApiCall { recapService.getActivityTrack(activityId) }
            .map { it.toActivityTrack() }
            .mapApiException(::mapRecapException)

    override suspend fun getRecapEditorData(activityId: Long): Result<RecapEditorData> = suspendRunCatching {
        coroutineScope {
            val resource = async { getRecapResource(activityId).getOrThrow() }
            val track = async { getActivityTrack(activityId).getOrThrow() }
            RecapEditorData(resource = resource.await(), track = track.await())
        }
    }

    private fun ActivityRecapResponse.toActivityRecap() = ActivityRecap(
        activityId = activityId,
        title = title,
        activityDate = LocalDate.parse(activityDate),
        timelineImageUrls = timelineImageUrls,
    )

    private fun RecapResourceResponse.toRecapResource() = RecapResource(
        distanceMeters = distance,
        durationSeconds = durationSec,
        maxElevationMeters = maxElevation,
        mapImageUrl = mapImageUrl,
        routes = routes.map { RecapRoute(sequence = it.sequence, name = it.name) },
    )

    private fun ActivityTrackResponse.toActivityTrack() = ActivityTrack(
        trackPoints = trackPoints.map {
            TrackPoint(
                latitude = it.latitude,
                longitude = it.longitude,
                elevation = it.elevation,
                pointIndex = it.pointIndex,
            )
        },
        timelineMarkers = timelineMarkers.map {
            TimelineMarker(
                timelineId = it.timelineId,
                thumbnailUrl = it.thumbnailUrl,
                latitude = it.latitude,
                longitude = it.longitude,
                pointIndex = it.pointIndex,
            )
        },
    )

    private fun mapRecapException(exception: ApiException): Throwable =
        if (exception is ApiException.NetworkConnection) {
            RecapException.NetworkUnavailable(
                exception.message,
            )
        } else {
            exception
        }
}
