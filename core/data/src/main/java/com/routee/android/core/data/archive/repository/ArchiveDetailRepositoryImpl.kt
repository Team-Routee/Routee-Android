package com.routee.android.core.data.archive.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.archive.model.ArchiveDetail
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.remote.service.ArchiveService
import com.routee.android.core.data.recap.model.RecapRoute
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class ArchiveDetailRepositoryImpl @Inject constructor(
    private val service: ArchiveService,
    private val recapRepository: RecapRepository,
    private val apiResponseHandler: ApiResponseHandler,
) : ArchiveDetailRepository {
    override suspend fun updateTitle(activityId: Long, title: String): Result<Unit> =
        recapRepository.updateRecapTitle(activityId, title)

    override suspend fun deleteActivity(activityId: Long): Result<Unit> =
        apiResponseHandler.safeUnitApiCall { service.deleteActivity(activityId) }

    override suspend fun getDetail(activityId: Long): Result<ArchiveDetail> = suspendRunCatching {
        coroutineScope {
            val statistics = async { apiResponseHandler.safeApiCall { service.getStatistics(activityId) }.getOrThrow() }
            val track = async { recapRepository.getActivityTrack(activityId).getOrThrow() }
            val timeline = async { apiResponseHandler.safeApiCall { service.getTimeline(activityId) }.getOrThrow() }
            val routes = async { apiResponseHandler.safeApiCall { service.getRoutes(activityId) }.getOrThrow() }
            val summary = statistics.await()
            ArchiveDetail(
                title = summary.title,
                date = LocalDate.parse(summary.activityDate),
                distanceMeters = summary.distanceMeter,
                durationSeconds = summary.durationSec,
                maxElevationMeters = summary.maxElevationMeter,
                track = track.await(),
                photos = timeline.await().timelines.map {
                    ArchivePhoto(it.timelineId, it.title, it.imageUrl, Instant.parse(it.createdAt))
                }.sortedBy { it.createdAt },
                routes = routes.await().routes.sortedBy { it.sequence }.map { RecapRoute(it.sequence, it.name) },
            )
        }
    }
}
