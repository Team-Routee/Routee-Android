package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelineCompletion
import com.routee.android.core.data.timeline.model.TimelineTrackPoint
import com.routee.android.core.data.timeline.remote.dto.request.ActivityCompleteRequest
import com.routee.android.core.data.timeline.remote.dto.request.RoutesCreateRequest
import com.routee.android.core.data.timeline.remote.service.TimelineCompletionService
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineCompletionRepositoryImplTest {
    @Test
    fun completeSavesRoutesAfterActivityWithCleanSequentialNames() = runTest {
        val service = FakeService()
        val repository = TimelineCompletionRepositoryImpl(service, ApiResponseHandler(Json))

        val result = repository.complete(input(listOf("  창의문 ", " ", "청운대", "창의문")), "map/key")

        assertTrue(result.isSuccess)
        assertEquals(listOf("complete", "routes"), service.calls)
        assertEquals("산행", service.completed?.title)
        assertEquals("map/key", service.completed?.mapImageObjectKey)
        assertEquals(listOf("창의문", "청운대", "창의문"), service.routes?.routes?.map { it.name })
        assertEquals(listOf(1, 2, 3), service.routes?.routes?.map { it.sequence })
    }

    @Test
    fun completeSkipsRoutesWhenEmptyAndStopsOnFailure() = runTest {
        val service = FakeService()
        val repository = TimelineCompletionRepositoryImpl(service, ApiResponseHandler(Json))

        assertTrue(repository.complete(input(emptyList()), "map/key").isSuccess)
        assertEquals(listOf("complete"), service.calls)

        service.calls.clear()
        service.failComplete = true
        val failingRepository = TimelineCompletionRepositoryImpl(service, ApiResponseHandler(Json))
        assertTrue(failingRepository.complete(input(listOf("창의문")), "map/key").isFailure)
        assertEquals(listOf("complete"), service.calls)
    }

    @Test
    fun retryAfterRouteFailureDoesNotCompleteActivityAgain() = runTest {
        val service = FakeService().apply { failRoutes = true }
        val repository = TimelineCompletionRepositoryImpl(service, ApiResponseHandler(Json))

        assertTrue(repository.complete(input(listOf("창의문")), "map/key").isFailure)
        service.failRoutes = false
        assertTrue(repository.complete(input(listOf("창의문")), "map/key").isSuccess)
        assertEquals(listOf("complete", "routes", "routes"), service.calls)
    }

    private fun input(names: List<String>) = TimelineCompletion(
        activityId = 1,
        title = "  산행  ",
        distanceMeters = 15000,
        durationSeconds = 3600,
        maxElevationMeters = 800,
        endedAtEpochMillis = 1_700_000_000_000,
        track = listOf(
            TimelineTrackPoint(37.0, 127.0, 100, 0),
            TimelineTrackPoint(37.1, 127.1, 200, 1),
        ),
        routeNames = names,
    )

    private class FakeService : TimelineCompletionService {
        val calls = mutableListOf<String>()
        var failComplete = false
        var failRoutes = false
        var completed: ActivityCompleteRequest? = null
        var routes: RoutesCreateRequest? = null

        override suspend fun complete(activityId: Long, request: ActivityCompleteRequest): BaseResponse<JsonElement> {
            calls += "complete"
            if (failComplete) throw IOException("offline")
            completed = request
            return response()
        }

        override suspend fun createRoutes(activityId: Long, request: RoutesCreateRequest): BaseResponse<JsonElement> {
            calls += "routes"
            if (failRoutes) throw IOException("offline")
            routes = request
            return response()
        }

        private fun response(): BaseResponse<JsonElement> =
            BaseResponse(status = 200, code = "SUCCESS", message = "OK")
    }
}
