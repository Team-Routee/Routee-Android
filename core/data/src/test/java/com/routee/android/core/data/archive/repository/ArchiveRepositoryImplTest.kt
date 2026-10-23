package com.routee.android.core.data.archive.repository

import com.routee.android.core.data.archive.remote.dto.response.MonthlyActivitySummaryResponse
import com.routee.android.core.data.archive.remote.service.ArchiveService
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchiveRepositoryImplTest {
    @Test
    fun summaryUsesServerResultAndKeepsMinuteUnitsAndCoverImage() = runTest {
        val service = FakeService()
        val result = ArchiveRepositoryImpl(service, ApiResponseHandler(Json))
            .getMonthlySummary(YearMonth.of(2026, 3)).getOrThrow()

        assertEquals(YearMonth.of(2026, 3), service.requestedMonth)
        assertEquals(LocalDate.of(2026, 3, 12), result.single().date)
        assertEquals(61, result.single().totalDurationMinutes)
        assertEquals(2, result.single().activityCount)
        assertEquals("photo", result.single().coverImageUrl)
    }

    @Test
    fun connectionFailureUsesCommonApiException() = runTest {
        val result = ArchiveRepositoryImpl(FakeService(IOException()), ApiResponseHandler(Json))
            .getMonthlySummary(YearMonth.of(2026, 3))

        assertTrue(result.exceptionOrNull() is ApiException.NetworkConnection)
    }

    @Test(expected = CancellationException::class)
    fun cancelledRequestIsNotTurnedIntoFailure() = runTest {
        ArchiveRepositoryImpl(FakeService(CancellationException()), ApiResponseHandler(Json))
            .getMonthlySummary(YearMonth.of(2026, 3))
    }

    private class FakeService(private val error: Exception? = null) : ArchiveService {
        var requestedMonth: YearMonth? = null

        override suspend fun getMonthlySummary(year: Int, month: Int): BaseResponse<MonthlyActivitySummaryResponse> {
            error?.let { throw it }
            requestedMonth = YearMonth.of(year, month)
            val response = Json.decodeFromString<MonthlyActivitySummaryResponse>(
                """{"result":[{"activityDate":"2026-03-12","totalDurationMinutes":61,
                    "activityCount":2,"coverImageUrl":"photo"}],"year":2026,"month":3}""",
            )
            return BaseResponse(status = 200, code = "OK", message = "", data = response)
        }
    }
}
