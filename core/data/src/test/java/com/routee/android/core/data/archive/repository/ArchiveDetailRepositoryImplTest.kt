package com.routee.android.core.data.archive.repository

import com.routee.android.core.data.archive.remote.dto.response.ActivityStatisticsResponse
import com.routee.android.core.data.archive.remote.dto.response.ArchiveRoutesResponse
import com.routee.android.core.data.archive.remote.dto.response.ArchiveTimelineResponse
import com.routee.android.core.data.archive.remote.service.ArchiveService
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchiveDetailRepositoryImplTest {
    @Test
    fun detailKeepsServerUnitsAndSortsPhotosAndRoutes() = runTest {
        val service = FakeService()
        val result = repository(service).getDetail(42).getOrThrow()

        assertEquals(setOf(42L), service.activityIds.toSet())
        assertEquals(LocalDate.of(2026, 3, 12), result.date)
        assertEquals(15530, result.distanceMeters)
        assertEquals(12000, result.durationSeconds)
        assertEquals(2132, result.maxElevationMeters)
        assertEquals(listOf(1L, 2L), result.photos.map { it.id })
        assertEquals(null, result.photos.first().location)
        assertEquals(listOf("첫 지점", "두 번째"), result.routes.map { it.name })
    }

    @Test
    fun failedRequestCancelsOtherRequestsAndKeepsNetworkFailure() = runTest {
        val service = FakeService().apply {
            failStatistics = true
            holdTimeline = true
        }
        val result = repository(service).getDetail(42)

        assertTrue(result.exceptionOrNull() is ApiException.NetworkConnection)
        assertTrue(service.timelineCancelled)
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsNotReturnedAsFailure() = runTest {
        val service = FakeService().apply { cancelStatistics = true }
        repository(service).getDetail(42)
    }

    private fun repository(service: ArchiveService) = ArchiveDetailRepositoryImpl(
        service,
        object : RecapRepository {
            override suspend fun getActivityTrack(
                activityId: Long,
            ) = Result.success(ActivityTrack(emptyList(), emptyList()))
            override suspend fun getMonthlyRecaps(month: java.time.YearMonth) = error("Unused")
            override suspend fun updateRecapTitle(activityId: Long, title: String) = error("Unused")
            override suspend fun getRecapResource(activityId: Long) = error("Unused")
            override suspend fun getRecapEditorData(activityId: Long) = error("Unused")
        },
        ApiResponseHandler(Json),
    )

    private class FakeService : ArchiveService {
        val activityIds = mutableListOf<Long>()
        var failStatistics = false
        var cancelStatistics = false
        var holdTimeline = false
        var timelineCancelled = false

        override suspend fun getMonthlySummary(year: Int, month: Int) = error("Unused")

        override suspend fun getStatistics(activityId: Long): BaseResponse<ActivityStatisticsResponse> {
            activityIds += activityId
            if (cancelStatistics) throw CancellationException()
            if (failStatistics) {
                delay(10)
                throw IOException()
            }
            return response(
                Json.decodeFromString<ActivityStatisticsResponse>(
                    """{"activityId":42,"title":"북악산","activityDate":"2026-03-12",
                    "distanceMeter":15530,"durationSec":12000,"maxElevationMeter":2132}""",
                ),
            )
        }

        override suspend fun getTimeline(activityId: Long): BaseResponse<ArchiveTimelineResponse> {
            activityIds += activityId
            if (holdTimeline) {
                try {
                    awaitCancellation()
                } finally {
                    timelineCancelled = true
                }
            }
            return response(
                Json.decodeFromString<ArchiveTimelineResponse>(
                    """{"activityId":42,"timelines":[
                    {"timelineId":2,"title":"정상","imageUrl":"second","createdAt":"2026-03-12T04:00:00Z"},
                    {"timelineId":1,"title":null,"imageUrl":"first","createdAt":"2026-03-12T03:00:00Z"}]}""",
                ),
            )
        }

        override suspend fun getRoutes(activityId: Long): BaseResponse<ArchiveRoutesResponse> {
            activityIds += activityId
            return response(
                Json.decodeFromString<ArchiveRoutesResponse>(
                    """{"activityId":42,"routes":[{"routeId":2,"name":"두 번째","sequence":2},
                    {"routeId":1,"name":"첫 지점","sequence":1}]}""",
                ),
            )
        }

        private fun <T> response(data: T) = BaseResponse(200, "OK", "", data)
    }
}
