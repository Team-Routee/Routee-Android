package com.routee.android.core.data.recap.repository

import com.routee.android.core.data.recap.exception.RecapException
import com.routee.android.core.data.recap.remote.dto.request.UpdateRecapTitleRequest
import com.routee.android.core.data.recap.remote.dto.response.ActivityTrackResponse
import com.routee.android.core.data.recap.remote.dto.response.MonthlyRecapResponse
import com.routee.android.core.data.recap.remote.dto.response.RecapResourceResponse
import com.routee.android.core.data.recap.remote.dto.response.RecapRouteResponse
import com.routee.android.core.data.recap.remote.service.RecapService
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecapRepositoryImplTest {
    @Test
    fun `리소스와 경로를 모두 불러오면 편집 데이터를 돌려준다`() = runTest {
        val data = repository(FakeRecapService()).getRecapEditorData(activityId = 1).getOrThrow()

        assertEquals(listOf("백운대"), data.resource.routes.map { it.name })
        assertTrue(data.track.trackPoints.isEmpty())
    }

    @Test
    fun `리소스 조회가 실패하면 경로 요청을 기다리지 않고 취소한다`() = runTest {
        val service = FakeRecapService(resourceError = IOException(), isTrackPending = true)

        val result = repository(service).getRecapEditorData(activityId = 1)

        assertTrue(result.isFailure)
        assertTrue(service.isTrackCancelled)
    }

    @Test
    fun `네트워크 끊김으로 실패하면 RecapException_NetworkUnavailable로 돌려준다`() = runTest {
        val result = repository(FakeRecapService(trackError = IOException())).getRecapEditorData(activityId = 1)

        assertTrue(result.exceptionOrNull() is RecapException.NetworkUnavailable)
    }

    private fun repository(service: RecapService) = RecapRepositoryImpl(
        recapService = service,
        apiResponseHandler = ApiResponseHandler(Json { ignoreUnknownKeys = true }),
    )

    private class FakeRecapService(
        private val resourceError: Throwable? = null,
        private val trackError: Throwable? = null,
        private val isTrackPending: Boolean = false,
    ) : RecapService {
        var isTrackCancelled = false

        override suspend fun getRecapResource(activityId: Long): BaseResponse<RecapResourceResponse> {
            delay(RESPONSE_DELAY_MILLIS)
            resourceError?.let { throw it }
            return success(
                RecapResourceResponse(
                    distance = 0,
                    durationSec = 0,
                    maxElevation = 0,
                    routes = listOf(RecapRouteResponse(sequence = 1, name = "백운대")),
                ),
            )
        }

        override suspend fun getActivityTrack(activityId: Long): BaseResponse<ActivityTrackResponse> {
            if (isTrackPending) {
                try {
                    awaitCancellation()
                } catch (e: CancellationException) {
                    isTrackCancelled = true
                    throw e
                }
            }
            trackError?.let { throw it }
            return success(ActivityTrackResponse(activityId = activityId))
        }

        override suspend fun getMonthlyRecaps(
            year: Int,
            month: Int,
        ): BaseResponse<MonthlyRecapResponse> = error("not used")

        override suspend fun updateRecapTitle(
            activityId: Long,
            request: UpdateRecapTitleRequest,
        ) = error("not used")

        private fun <T> success(data: T) = BaseResponse(status = 200, code = "OK", message = "", data = data)
    }

    private companion object {
        const val RESPONSE_DELAY_MILLIS = 100L
    }
}
