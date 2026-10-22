package com.routee.android.core.domain.recap

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapEditorData
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.repository.RecapRepository
import java.io.IOException
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRecapEditorDataUseCaseTest {
    @Test
    fun `리소스와 경로를 모두 불러오면 성공한다`() = runTest {
        val result = GetRecapEditorDataUseCase(FakeRecapRepository()).invoke(activityId = 1)

        assertEquals(RecapEditorData(resource = RESOURCE, track = TRACK), result.getOrNull())
    }

    @Test
    fun `리소스 조회가 실패하면 경로 요청을 기다리지 않고 취소한다`() = runTest {
        val repository = FakeRecapRepository(resourceError = IOException(), isTrackPending = true)

        val result = GetRecapEditorDataUseCase(repository).invoke(activityId = 1)

        assertTrue(result.exceptionOrNull() is IOException)
        assertTrue(repository.isTrackCancelled)
    }

    @Test
    fun `경로 조회가 실패하면 실패를 돌려준다`() = runTest {
        val result = GetRecapEditorDataUseCase(FakeRecapRepository(trackError = IOException())).invoke(activityId = 1)

        assertTrue(result.exceptionOrNull() is IOException)
    }

    private class FakeRecapRepository(
        private val resourceError: Throwable? = null,
        private val trackError: Throwable? = null,
        private val isTrackPending: Boolean = false,
    ) : RecapRepository {
        var isTrackCancelled = false

        override suspend fun getRecapResource(activityId: Long): Result<RecapResource> {
            delay(RESPONSE_DELAY_MILLIS)
            return resourceError?.let { Result.failure(it) } ?: Result.success(RESOURCE)
        }

        override suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack> {
            if (isTrackPending) {
                try {
                    awaitCancellation()
                } catch (e: CancellationException) {
                    isTrackCancelled = true
                    throw e
                }
            }
            return trackError?.let { Result.failure(it) } ?: Result.success(TRACK)
        }

        override suspend fun getRecapEditorData(activityId: Long): Result<RecapEditorData> = error("not used")

        override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> = Result.success(
            emptyList(),
        )

        override suspend fun updateRecapTitle(
            activityId: Long,
            title: String,
        ): Result<Unit> = Result.success(Unit)
    }

    private companion object {
        const val RESPONSE_DELAY_MILLIS = 100L
        val RESOURCE = RecapResource(
            distanceMeters = 0,
            durationSeconds = 0,
            maxElevationMeters = 0,
            mapImageUrl = null,
            routes = emptyList(),
        )
        val TRACK = ActivityTrack(trackPoints = emptyList(), timelineMarkers = emptyList())
    }
}
